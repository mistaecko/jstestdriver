/*
 * Copyright 2009 Google Inc.
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

import static java.lang.String.format;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.jstestdriver.Response.ResponseType;
import com.google.jstestdriver.browser.BrowserPanicException;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.util.StopWatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * Handles the communication of a command to the JsTestDriverServer from the
 * JsTestDriverClient.
 *
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class CommandTask {

  private static final Logger logger = LoggerFactory.getLogger(CommandTask.class);

  private final Gson gson = new Gson();

  private final ResponseStream stream;
  private final String baseUrl;
  private final Server server;
  private final Map<String, String> params;
  private final boolean upload;

  private final StopWatch stopWatch;

  private final FileUploader fileUploader;


  public CommandTask(
      ResponseStream stream,
      String baseUrl,
      Server server,
      Map<String, String> params,
      boolean upload,
      StopWatch stopWatch,
      FileUploader uploader) {
    this.stream = stream;
    this.baseUrl = baseUrl;
    this.server = server;
    this.params = params;
    this.upload = upload;
    this.stopWatch = stopWatch;
    this.fileUploader = uploader;
  }
  
  /**
   * Throws an exception if the expected browser is not available for this task.
   */
  private void checkBrowser() {
    String alive = server.fetch(baseUrl + "/heartbeat?id=" + params.get("id"));

    if (!alive.equals("OK")) {
      throw new FailureException(
          format("Browser is not available\n %s \n for\n %s", alive, params));
    }
  }
  
  private BrowserInfo getBrowser(String id) {
    Collection<BrowserInfo> browsers = gson.fromJson(server.fetch(baseUrl + "/cmd?listBrowsers"),
        new TypeToken<Collection<BrowserInfo>>() {}.getType());
    for (BrowserInfo browser : browsers) {
      if (id.equals(String.valueOf(browser.getId()))) {
        return browser;
      }
    }
    return null;
  }

  public void run(JstdTestCase testCase) {
    stopWatch.start("run %s", testCase.getId());
    String browserId = params.get("id");
    try {
      stopWatch.start("checkBrowser %s", browserId);
      checkBrowser();
      stopWatch.stop("checkBrowser %s", browserId);
      logger.debug("Starting upload for {}", browserId);
      // TODO(corysmith): Move the loading of files to a browser into the server.
      if (upload) {
        fileUploader.uploadToTheBrowser(
            browserId,
            stream,
            fileUploader.determineBrowserFileSet(
                browserId,
                testCase,
                stream),
        getBrowser(browserId).getUploadSize());
      }
      logger.debug("Finished upload for {}", browserId);
      stopWatch.start("post %s", params);
      server.post(baseUrl + "/cmd", params);
      stopWatch.stop("post %s", params);
    } finally {
      
    }
    try {
      logger.debug("Starting {} for {}", params.get("data"), browserId);
      stopWatch.start("execution %s", params.get("data"));
      StreamMessage streamMessage = null;
      do {
        String response = server.fetch(baseUrl + "/cmd?id=" + browserId);
        try {
          streamMessage = gson.fromJson(response, StreamMessage.class);
        } catch (Exception e) {
          throw new RuntimeException("Error deserializing: " + response, e);
        }
        Response resObj = streamMessage.getResponse();
        if (ResponseType.BROWSER_PANIC.equals(resObj.getResponseType())) {
          throw new BrowserPanicException(resObj.getBrowser(), resObj.getResponse());
        }
        stream.stream(resObj);
      } while (!streamMessage.isLast());
      logger.debug("finished {} for {} with {}", new Object[] {params.get("data"), browserId, streamMessage.getResponse().getResponseType()});
    } finally {
      stopWatch.stop("execution %s", params.get("data"));
      logger.debug("finished {} for {}", params.get("data"), browserId);
      stopWatch.stop("run %s", testCase.getId());
    }
  }
}
