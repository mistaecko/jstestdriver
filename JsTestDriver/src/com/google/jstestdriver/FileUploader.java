/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.jstestdriver;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.jstestdriver.JsonCommand.CommandType;
import com.google.jstestdriver.Response.ResponseType;
import com.google.jstestdriver.browser.BrowserFileSet;
import com.google.jstestdriver.hooks.FileInfoScheme;
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.model.JstdTestCaseDelta;
import com.google.jstestdriver.servlet.fileset.BrowserFileCheck;
import com.google.jstestdriver.servlet.fileset.DeltaUpload;
import com.google.jstestdriver.servlet.fileset.TestCaseUpload;
import com.google.jstestdriver.util.StopWatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles the uploading of files.
 *
 * @author corysmith@google.com (Cory Smith)
 */
public class FileUploader {
  public static final int CHUNK_SIZE = 50;

  private final StopWatch stopWatch;
  private final Gson gson = new Gson();
  private final Server server;
  private final String baseUrl;
  private final FileLoader fileLoader;
  private final JsTestDriverFileFilter filter;
  private final Set<FileInfoScheme> schemes;

  private static final Logger logger = LoggerFactory.getLogger(FileUploader.class);

  private final HandlerPathPrefix prefix;

  @Inject
  public FileUploader(StopWatch stopWatch, Server server,
      @Named("server") String baseUrl, FileLoader fileLoader,
      JsTestDriverFileFilter filter,
      Set<FileInfoScheme> schemes,
      @Named("serverHandlerPrefix") HandlerPathPrefix prefix) {
    this.stopWatch = stopWatch;
    this.server = server;
    this.baseUrl = baseUrl;
    this.fileLoader = fileLoader;
    this.filter = filter;
    this.schemes = schemes;
    this.prefix = prefix;
  }

  /** Uploads the changed files to the server and the browser. */
  public void uploadFileSet(String browserId, Collection<JstdTestCase> testCases, ResponseStream stream) {

    stopWatch.start("determineServerFileSet(%s)", browserId);
    final Collection<JstdTestCaseDelta> deltas = determineServerFileSet(testCases);
    stopWatch.stop("determineServerFileSet(%s)", browserId);

    logger.debug("Deltas: {}", deltas);
    stopWatch.start("upload to server %s", browserId);
    uploadToServer(deltas);
    stopWatch.stop("upload to server %s", browserId);
    for (JstdTestCase testCase : testCases) {
      stopWatch.start("determineBrowserFileSet(%s)", browserId);
      final List<FileInfo> browserFilesToUpdate = determineBrowserFileSet(browserId, testCase, stream);
      stopWatch.stop("determineBrowserFileSet(%s)", browserId);

      stopWatch.start("uploadToTheBrowser(%s)", browserId);
      uploadToTheBrowser(browserId, stream, browserFilesToUpdate, CHUNK_SIZE);
      stopWatch.stop("uploadToTheBrowser(%s)", browserId);
    }
  }
  

  /**
   * Uploads the {@link JstdTestCase}s to the server, and retrieves a list
   * of {@link JstdTestCaseDelta}s of the files that are different.
   */
  public Collection<JstdTestCaseDelta> determineServerFileSet(Collection<JstdTestCase> testCases) {
    Map<String, String> fileSetParams = new LinkedHashMap<String, String>();
    fileSetParams.put("data", gson.toJson(testCases));
    fileSetParams.put("action", TestCaseUpload.ACTION);
    String postResult = server.post(baseUrl + "/fileSet", fileSetParams);
    return gson.fromJson(postResult, new TypeToken<Collection<JstdTestCaseDelta>>() {}.getType());
  }

  /** Determines what files have been changed as compared to the server. */
  public List<FileInfo> determineBrowserFileSet(String browserId, JstdTestCase testCase,
      ResponseStream stream) {

    BrowserFileSet browserFileSet = getBrowserFileSet(browserId, testCase);

    stopWatch.start("resolving browser upload %s", browserId);
    try {
      logger.debug("Updating files {}", browserFileSet.getFilesToUpload());
      // need a linked hashset here to avoid adding a file more than once.
      final Set<FileInfo> finalFilesToUpload = new LinkedHashSet<FileInfo>();
      // reset if there are extra files in the browser
      if (browserFileSet.shouldReset() || !browserFileSet.getExtraFiles().isEmpty()) {
        reset(browserId, stream, testCase);
        browserFileSet = getBrowserFileSet(browserId, testCase);
        logger.info("second fileset {}", browserFileSet);
      }
      for (FileInfo file : browserFileSet.getFilesToUpload()) {
        finalFilesToUpload.addAll(determineInBrowserDependencies(file, testCase.getServable()));
      }
      return Lists.newArrayList(finalFilesToUpload);
    } finally {
      stopWatch.stop("resolving browser upload %s", browserId);
    }
  }

  private BrowserFileSet getBrowserFileSet(String browserId, JstdTestCase testCase) {
    stopWatch.start("get upload set %s", browserId);
    Map<String, String> fileSetParams = new LinkedHashMap<String, String>();

    fileSetParams.put("id", browserId);
    fileSetParams.put("data", gson.toJson(testCase));
    fileSetParams.put("action", BrowserFileCheck.ACTION);
    //logger.info("FileParams: {}", fileSetParams);
    String postResult = server.post(baseUrl + "/fileSet", fileSetParams);
    if (postResult.length() < 0) {
      return new BrowserFileSet(Collections.<FileInfo>emptyList(), Collections.<FileInfo>emptyList(), false);
    }
    BrowserFileSet browserFileSet = gson.fromJson(postResult, BrowserFileSet.class);
    stopWatch.stop("get upload set %s", browserId);
    return browserFileSet;
  }


  /** Uploads files to the browser. */
  public void uploadToTheBrowser(String browserId, ResponseStream stream,
      List<FileInfo> loadedFiles, int chunkSize) {
    List<FileSource> filesSrc = Lists.newLinkedList(filterFilesToLoad(loadedFiles));
    int numberOfFilesToLoad = filesSrc.size();
    logger.info("Files toupload {}",
        Lists.transform(Lists.newArrayList(loadedFiles), new Function<FileInfo, String>() {
          @Override
          public String apply(FileInfo in) {
            return "\n" + in.getDisplayPath();
          }
        }));
    for (int i = 0; i < numberOfFilesToLoad; i += chunkSize) {

      int chunkEndIndex = Math.min(i + chunkSize, numberOfFilesToLoad);
      List<String> loadParameters = new LinkedList<String>();
      List<FileSource> filesToLoad = filesSrc.subList(i, chunkEndIndex);
      loadParameters.add(gson.toJson(filesToLoad));
      loadParameters.add("false");
      JsonCommand cmd = new JsonCommand(CommandType.LOADTEST, loadParameters);
      Map<String, String> loadFileParams = new LinkedHashMap<String, String>();

      loadFileParams.put("id", browserId);
      loadFileParams.put("data", gson.toJson(cmd));
      if (logger.isDebugEnabled()) {
        logger.debug("Sending LOADTEST to {} for {}", browserId,
            Lists.transform(filesToLoad, new Function<FileSource, String>() {
              @Override
              public String apply(FileSource in) {
                return "\n" + in.getFileSrc();
              }
            }));
      }
      server.post(baseUrl + "/cmd", loadFileParams);
      while (true) {
        String jsonResponse = server.fetch(baseUrl + "/cmd?id=" + browserId);
        StreamMessage message = gson.fromJson(jsonResponse, StreamMessage.class);
        Response response = message.getResponse();
        logger.trace("LOADTEST response for {}", response);
        stream.stream(response);
        if (message.isLast()) {
          logger.debug("Finished LOADTEST on {} with {}", browserId, response.getResponseType());
          break;
        }
      }
    }
    
  }

  public void uploadToServer(final Collection<JstdTestCaseDelta> deltas) {
    if (deltas.isEmpty()) {
      return;
    }
    List<JstdTestCaseDelta> loadedDeltas = Lists.newArrayListWithCapacity(deltas.size());
    for (JstdTestCaseDelta delta : deltas) {
      loadedDeltas.add(delta.loadFiles(fileLoader));
    }
    Map<String, String> uploadFileParams = new LinkedHashMap<String, String>();
    uploadFileParams.put("action", DeltaUpload.ACTION);
    uploadFileParams.put("data", gson.toJson(loadedDeltas));
    server.post(baseUrl + "/fileSet", uploadFileParams);
  }

  private void reset(String browserId, ResponseStream stream, JstdTestCase testCase) {
    stopWatch.start("reset %s", browserId);
    JsonCommand cmd = new JsonCommand(CommandType.RESET,
        Lists.newArrayList("preload", testCase.getId()));
    Map<String, String> resetParams = new LinkedHashMap<String, String>();

    logger.debug("reset browser {}  testcase {}", browserId, testCase.getId());
    resetParams.put("id", browserId);
    resetParams.put("data", gson.toJson(cmd));
    server.post(baseUrl + "/cmd", resetParams);

    logger.trace("starting reset for {}", browserId);
    Response response;
    StreamMessage message;
    do {
      String jsonResponse = server.fetch(baseUrl + "/cmd?id=" + browserId);
      message = gson.fromJson(jsonResponse, StreamMessage.class);
      response = message.getResponse();
      stream.stream(response);
    } while(!(ResponseType.RESET_RESULT.equals(response.getResponseType()) && message.isLast()));
    logger.trace("finished reset for {}", browserId);
    stopWatch.stop("reset %s", browserId);
  }

  /**
   * Determines what files must be reloaded in the browser, based on this file
   * being updated.
   */
  private Collection<FileInfo> determineInBrowserDependencies(FileInfo file, List<FileInfo> files) {
    LinkedHashSet<FileInfo> deps = Sets.newLinkedHashSet();
    for (FileInfo dep : filter.resolveFilesDeps(file, files)) {
      deps.add(dep);
    }
    return deps;
  }

  private List<FileSource> filterFilesToLoad(Collection<FileInfo> fileInfos) {
    List<FileSource> filteredFileSources = new LinkedList<FileSource>();

    for (FileInfo fileInfo : fileInfos) {
      if (!fileInfo.isServeOnly()) {
        filteredFileSources.add(fileInfo.toFileSource(prefix, schemes));
      }
    }
    return filteredFileSources;
  }
}
