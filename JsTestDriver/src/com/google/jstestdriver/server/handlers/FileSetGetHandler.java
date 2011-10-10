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
package com.google.jstestdriver.server.handlers;

import com.google.inject.Inject;
import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.Lock;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.Time;
import com.google.jstestdriver.annotations.ResponseWriter;
import com.google.jstestdriver.requesthandlers.RequestHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
class FileSetGetHandler implements RequestHandler {
  private static final Logger logger =
      LoggerFactory.getLogger(FileSetGetHandler.class);

  private static final int HEARTBEAT_TIMEOUT = 2000;

  private final HttpServletRequest request;
  private final PrintWriter writer;

  private final CapturedBrowsers capturedBrowsers;

  private final Time time;

  @Inject
  public FileSetGetHandler(
      HttpServletRequest request,
      @ResponseWriter PrintWriter writer,
      CapturedBrowsers capturedBrowsers,
      Time time) {
    this.request = request;
    this.writer = writer;
    this.capturedBrowsers = capturedBrowsers;
    this.time = time;
  }

  @SuppressWarnings("unused")
  @Override
  public void handleIt() throws IOException {
    String id = request.getParameter("id");
    String session = request.getParameter("session");
    String sessionId = request.getParameter("sessionId");
    System.out.printf("\nid %s session %s sessionId %s", id, session, sessionId);

    if (session == null && sessionId != null) {
      sessionHeartBeat(id, sessionId);
    } else {
      if (session.equals("start")) {
        startSession(id, writer);
      } else if (session.equals("stop")) {
        stopSession(id, sessionId, writer);
      }
    }
  }

  private void sessionHeartBeat(String id, String sessionId) {
    SlaveBrowser browser = capturedBrowsers.getBrowser(id);
    if (browser == null) {
      logger.error("heartbeat to a dead session");
      return;
    }
    browser.heartBeatLock(sessionId);
  }

  public void stopSession(String id, String sessionId, PrintWriter writer) {
    SlaveBrowser browser = capturedBrowsers.getBrowser(id);
    try {
      browser.unlock(sessionId);
      browser.clearCommandRunning();
    } finally {
      writer.flush();
    }
  }

  public void startSession(String id, PrintWriter writer) {
    logger.debug("trying to start session for {}", id);
    System.out.printf("\ntrying to start session for %s", id);
    SlaveBrowser browser = capturedBrowsers.getBrowser(id);
    String sessionId = UUID.randomUUID().toString();
    SlaveBrowser slaveBrowser = capturedBrowsers.getBrowser(id);

    if (browser.tryLock(sessionId)) {
      System.out.printf("\ngot session lock %s", sessionId);
      logger.debug("got session lock {} for {}", sessionId, id);
      writer.write(sessionId);
      slaveBrowser.resetCommandQueue();
      slaveBrowser.clearResponseQueue();
      browser.heartBeatLock(sessionId);
    } else {
      System.out.printf("\nrequest forceUnlock for %s", sessionId);
      logger.debug("checking session status for {}", id);
      // session is probably stalled
      if (!browser.inUse()) {
        System.out.printf("\nforcing unlock for %s", id);
        logger.debug("forcing unlock for {}", id);
        browser.forceUnlock();

        slaveBrowser.resetCommandQueue();
        slaveBrowser.clearResponseQueue();
        writer.write(browser.tryLock(sessionId) ? sessionId : "FAILED");
      } else {
        logger.debug("session unvailable for {}", id);
        writer.write("FAILED");
      }
    }
    writer.flush();
  }
}