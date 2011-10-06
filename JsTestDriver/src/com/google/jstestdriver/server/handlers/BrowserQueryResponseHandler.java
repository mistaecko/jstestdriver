/*
 * Copyright 2008 Google Inc.
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
package com.google.jstestdriver.server.handlers;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.inject.Inject;
import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.Command;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FileResult;
import com.google.jstestdriver.FileSource;
import com.google.jstestdriver.JsonCommand;
import com.google.jstestdriver.LoadedFiles;
import com.google.jstestdriver.Response;
import com.google.jstestdriver.Response.ResponseType;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.protocol.BrowserLog;
import com.google.jstestdriver.protocol.BrowserStreamAcknowledged;
import com.google.jstestdriver.requesthandlers.RequestHandler;

import org.mortbay.jetty.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
class BrowserQueryResponseHandler implements RequestHandler {
  private static final Logger logger =
      LoggerFactory.getLogger(BrowserQueryResponseHandler.class);

  private final Gson gson = new Gson();

  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final CapturedBrowsers browsers;
  // TODO(corysmith): factor out a streaming session class.
  private final ConcurrentMap<SlaveBrowser, List<String>> streamedResponses;

  @Inject
  public BrowserQueryResponseHandler(
      HttpServletRequest request,
      HttpServletResponse response,
      CapturedBrowsers browsers,
      ConcurrentMap<SlaveBrowser, List<String>> streamedResponses) {
    this.request = request;
    this.response = response;
    this.browsers = browsers;
    this.streamedResponses = streamedResponses;
  }

  @Override
  public void handleIt() throws IOException {
    logger.trace("Browser Query Post:\n\tpath:{}\n\tresponse:{}\n\tdone:{}\n\tresponseId:{}",
        new Object[] {
          request.getPathInfo().substring(1),
          request.getParameter("response"),
          request.getParameter("done"),
          request.getParameter("responseId")
        });

    response.setContentType(MimeTypes.TEXT_JSON_UTF_8);
    service(request.getPathInfo().substring(1),
            request.getParameter("response"),
            request.getParameter("done"),
            request.getParameter("responseId"),
            response.getWriter());
  }

  public void service(String id,
                      String response,
                      String done,
                      String responseId,
                      PrintWriter writer) {
    SlaveBrowser browser = browsers.getBrowser(id);

    if (browser != null) {
      boolean isLast = Boolean.parseBoolean(done);
      try {
        serviceBrowser(response, isLast, responseId, writer, browser);
      } catch (JsonParseException e) {
        writer.print(gson.toJson(new JsonCommand(JsonCommand.CommandType.STOP, null)));
        writer.flush();
        throw new RuntimeException("Unable to parse: " + response, e);
      }
    } else {
      // TODO(corysmith): handle this better.
      logger.error("Unknown browser {} with response {}.\n Known {}", new Object[]{id, response, browsers.getSlaveBrowsers()});
      writer.print(gson.toJson(new JsonCommand(JsonCommand.CommandType.STOP, Lists.newArrayList("Stopping due to missing browser."))));
      try {
        Thread.sleep(1000); // pause to make sure the browser doesn't spin.
      } catch (InterruptedException e) {
        
      }
    }
    writer.flush();
  }

  private void serviceBrowser(String response, Boolean done, String responseId, PrintWriter writer,
      SlaveBrowser browser) throws JsonParseException {
    addResponseId(responseId, browser);
    browser.heartBeat();
    Command command = null;
    if (isResponseValid(response)) {
      Response res = gson.fromJson(response, Response.class);
      logger.trace("response type: " +  res.getResponseType());
      // TODO (corysmith): Replace this with polymorphism,
      // using the response type to create disposable actions.
      switch (res.getResponseType()) {
        case BROWSER_READY:
          handleFileLoadResult(browser, res);
          // TODO(corysmith): Move the loading of files to a browser into the
          // server
          browser.addResponse(
              new Response(ResponseType.FILE_LOAD_RESULT.toString(), res.getResponse(), browser
                  .getBrowserInfo(), "", res.getExecutionTime()), false);
          browser.ready();
          break;
        case FILE_LOAD_RESULT:
          handleFileLoadResult(browser, res);
          browser.addResponse(res, done);
          break;
        case NOOP:
          break;
        case LOG:
          BrowserLog log = gson.fromJson(res.getResponse(), res.getGsonType());
          if (log.getLevel() == 1000) {
            logger.info("Error in browser: " + res.toString());
          } else {
            logger.info("Message from the browser: " + res.toString());
          }
          browser.addResponse(res, done);
          break;
        // reset the browsers fileset.
        case RESET_RESULT:
          browser.resetFileSet();
          logger.debug("Clearing fileset for {}", browser);
          handleFileLoadResult(browser, res);
          // queue the load results for the next command to be run.
          browser.addResponse(
              new Response(ResponseType.FILE_LOAD_RESULT.toString(), res.getResponse(), browser
                  .getBrowserInfo(), "", res.getExecutionTime()), false);
          browser.addResponse(res, done);
          break;
        case UNKNOWN:
          logger.error("Recieved Unknown: " + response);
          browser.addResponse(res, done);
          break;
        default:
          browser.addResponse(res, done);
          break;
      }
      logger.trace("Received:\n done: {} \n res:\n {}\n", new Object[] {done, res});
    }
    if (isResponseIdValid(responseId) && !done && !isResponseValid(response)) {
      logger.trace("Streaming query for ids {} from {}", streamedResponses.get(browser), browser);
    }
    // TODO(corysmith): What do we do?
    if (!isResponseValid(response) && done && browser.isCommandRunning()) {
      logger.error("Streaming ending, but no response sent for {} while running {}",
          browser,
          browser.getCommandRunning());
    }
    // TODO(corysmith): Refactoring the streaming into a separate layer.
    if (!done) { // we are still streaming, so we respond with the streaming
                 // acknowledge.
      // this is independent of receiving an actual response.
      final String jsonResponse = gson.toJson(new BrowserStreamAcknowledged(streamedResponses.get(browser)));
      logger.trace("sending jsonResponse {}", jsonResponse);
      writer.print(jsonResponse);
      writer.flush();
      return;
    } else {
      streamedResponses.clear();
    }
    if(command == null) {
     command = browser.dequeueCommand();
     browser.heartBeat();
    }
    
    logger.trace("sending command {}", command == null ? "null" : command.getCommand());
    writer.print(command.getCommand());
  }

  /**
   * @param browser
   * @param res
   */
  private void handleFileLoadResult(SlaveBrowser browser, Response res) {
    LoadedFiles loadedFiles = gson.fromJson(res.getResponse(), res.getGsonType());
    Collection<FileResult> allLoadedFiles = loadedFiles.getLoadedFiles();
    logger.info("loaded {} files", allLoadedFiles.size());
    if (!allLoadedFiles.isEmpty()) {
      LinkedHashSet<FileInfo> fileInfos = new LinkedHashSet<FileInfo>();
      Collection<FileSource> errorFiles = new LinkedHashSet<FileSource>();

      for (FileResult fileResult : allLoadedFiles) {
        FileSource fileSource = fileResult.getFileSource();

        if (fileResult.isSuccess()) {
          fileInfos.add(fileSource.toFileInfo(null));
        } else {
          errorFiles.add(fileSource);
        }
      }
      browser.addFiles(fileInfos, loadedFiles);
      if (errorFiles.size() > 0) {
        logger.info("clearing fileset on browser errors:" + errorFiles);
        browser.resetFileSet();
      }
    }
  }

  private boolean isResponseValid(String response) {
    return response != null && !"null".equals(response) && !"undefined".equals(response) && response.length() > 0;
  }

  private void addResponseId(String responseId, SlaveBrowser browser) {
    if (!streamedResponses.containsKey(browser)) {
      streamedResponses.put(browser, new CopyOnWriteArrayList<String>());
    }
    if (isResponseIdValid(responseId)) {
      return;
    }
    streamedResponses.get(browser).add(responseId);
  }

  private boolean isResponseIdValid(String responseId) {
    return responseId == null || "".equals(responseId);
  }
}