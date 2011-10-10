// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.server.handlers;

import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.MockTime;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.SlaveBrowser.BrowserState;
import com.google.jstestdriver.Time;
import com.google.jstestdriver.browser.BrowserIdStrategy;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.runner.RunnerType;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;



/**
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class FileSetGetHandlerTest extends TestCase {
  public void testQueueingSingleThreaded() throws Exception {
    String browserId = "foo";
    MockTime time = new MockTime(System.currentTimeMillis());
    SlaveBrowser browser = new SlaveBrowser(time, browserId, new BrowserInfo(), 10000, new NullPathPrefix(), "quirks", RunnerType.CLIENT, BrowserState.READY);
    CapturedBrowsers capturedBrowsers = new CapturedBrowsers(
        new BrowserIdStrategy(time));
    capturedBrowsers.addSlave(browser);

    String sessionIdOne = doRequest(browserId, null, "start", capturedBrowsers, time);
    assertNotNull("Expected the response to be not null.", sessionIdOne);
    assertFalse("Expected sessionId, got failed", "FAILED".equals(sessionIdOne));

    String failed = doRequest(browserId, null, "start", capturedBrowsers, time);
    assertTrue("Expected to be failed, but got:" + failed, "FAILED".equals(failed));
    for (int i = 0; i < 4; i++) {
      time.add(SlaveBrowser.SESSION_TIMEOUT - 1);
      //heartbeat
      doRequest(browserId, sessionIdOne, null, capturedBrowsers, time);
      failed = doRequest(browserId, null, "start", capturedBrowsers, time);
      assertTrue("Expected to be failed because the session is active, but got:" + failed, "FAILED".equals(failed));
    }
    doRequest(browserId, sessionIdOne, "stop", capturedBrowsers, time);
    String sessionIdTwo = doRequest(browserId, null, "start", capturedBrowsers, time);
    assertNotNull("Expected the response to be not null.", sessionIdTwo);
    assertFalse("Expected sessionId, got failed", "FAILED".equals(sessionIdTwo));
  }

  public void testQueueingSingleThreadedTimeoutForceUnlock() throws Exception {
    String browserId = "foo";
    MockTime time = new MockTime(System.currentTimeMillis());
    SlaveBrowser browser =
        new SlaveBrowser(time, browserId, new BrowserInfo(), 10000, new NullPathPrefix(), "quirks",
            RunnerType.CLIENT, BrowserState.READY);
    CapturedBrowsers capturedBrowsers = new CapturedBrowsers(new BrowserIdStrategy(time));
    capturedBrowsers.addSlave(browser);

    String sessionIdOne = doRequest(browserId, null, "start", capturedBrowsers, time);
    assertNotNull("Expected the response to be not null.", sessionIdOne);
    assertFalse("Expected sessionId, got failed", "FAILED".equals(sessionIdOne));

    String failed = doRequest(browserId, null, "start", capturedBrowsers, time);
    assertTrue("Expected to be failed, but got:" + failed, "FAILED".equals(failed));
    time.add(SlaveBrowser.SESSION_TIMEOUT + 2);
    String sessionIdTwo = doRequest(browserId, null, "start", capturedBrowsers, time);
    assertNotNull("Expected the response to be not null.", sessionIdTwo);
    assertFalse("Expected sessionId, got failed", "FAILED".equals(sessionIdTwo));
  }


  private String doRequest(String id, String sessionId, String session,
      CapturedBrowsers capturedBrowsers, Time time) throws IOException {
    FakeHttpServletRequest request = new FakeHttpServletRequest();
    request.setParameter("id", id);
    request.setParameter("session", session);
    request.setParameter("sessionId", sessionId);

    StringWriter stringWriter = new StringWriter();
    new FileSetGetHandler(request, new PrintWriter(stringWriter), capturedBrowsers, time)
        .handleIt();
    return stringWriter.toString();
  }
}
