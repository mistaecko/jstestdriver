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
package com.google.jstestdriver;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.jstestdriver.browser.BrowserIdStrategy;
import com.google.jstestdriver.hooks.FileInfoScheme;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.util.NullStopWatch;

import junit.framework.TestCase;

import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class JsTestDriverServerTest extends TestCase {

  private CapturedBrowsers browsers = new CapturedBrowsers(new BrowserIdStrategy(new MockTime(1)));

  private JsTestDriverServerImpl server;

  private JsTestDriverServerImpl createServer(ServerListener listener) {
    server = new JsTestDriverServerImpl(4224,
        new FilesCache(new HashMap<String, FileInfo>()),
        browsers,
        SlaveBrowser.TIMEOUT,
        new NullPathPrefix(),
        Sets.newHashSet(listener));
    return server;
  }

  @Override
  protected void tearDown() throws Exception {
    if (server != null) {
      server.stop();
      server = null;
    }
  }

  public void testServerLifecycle() throws Exception {
    TestListener listener = new TestListener();
    createServer(listener);
    server.start();
    URL url = new URL("http://localhost:4224/hello");
    assertEquals("hello", read(url.openStream()));
  }

  private String read(InputStream inputStream) throws IOException {
    StringBuilder builder = new StringBuilder();
    int ch;

    while ((ch = inputStream.read()) != -1) {
      builder.append((char) ch);
    }
    return builder.toString();
  }

  public void testStaticFiles() throws Exception {
    TestListener listener = new TestListener();
    createServer(listener);
    server.start();
    URL url = new URL("http://localhost:4224/static/Namespace.js");
    assertTrue(read(url.openStream()).length() > 0);
  }

  public void testCapture() throws Exception {
    TestListener listener = new TestListener();
    createServer(listener);
    server.start();
    URL captureUrl = new URL("http://localhost:4224/capture");
    assertTrue(read(captureUrl.openStream()).length() > 0);
    assertEquals(1, browsers.getBrowsers().size());
    assertEquals(1, browsers.getBrowsers().get(0).getId().intValue());
    assertEquals(new Long(1), listener.captured.getId());
  }

  public void testCaptureWithId() throws Exception {
    TestListener listener = new TestListener();
    createServer(listener);
    server.start();
    URL captureUrl = new URL("http://localhost:4224/capture?id=5");
    assertTrue(read(captureUrl.openStream()).length() > 0);
    assertEquals(1, browsers.getBrowsers().size());
    assertEquals(5, browsers.getBrowsers().get(0).getId().intValue());
    assertEquals(new Long(5), listener.captured.getId());
  }

  public void testListBrowsers() throws Exception {
    final NullStopWatch stopWatch = new NullStopWatch();
    JsTestDriverClient client = new JsTestDriverClientImpl(
        new CommandTaskFactory(
            new DefaultFileFilter(),
            null,
            null,
            stopWatch,
           ImmutableSet.<FileInfoScheme>of(new HttpFileInfoScheme()),
           new NullPathPrefix()),
        "http://localhost:4224",
        new HttpServer(),
        false,
        null);

    TestListener listener = new TestListener();
    createServer(listener);
    server.start();
    Collection<BrowserInfo> browsers = client.listBrowsers();
    assertEquals(0, browsers.size());
  }

  public void testShouldNotifyObserversOnServerStart() throws Exception {
    TestListener listener = new TestListener();
    createServer(listener);

    server.start();
    assertTrue(listener.started);
  }

  public void testShouldNotifyObserversOnServerStop() throws Exception {
    TestListener listener = new TestListener();
    createServer(listener);
    server.start();
    assertTrue(listener.started);
    server.stop();
    assertTrue(listener.stopped);
  }

  public void testGetShouldNotBeSentAsPost() throws Exception {
    createServer(new TestListener());
    server.start();
    JsonObject entry = new JsonObject();
    entry.addProperty("matcher", "/*");
    entry.addProperty("server", "http://localhost:8888/");
    JsonArray proxyConfig = new JsonArray();
    proxyConfig.add(entry);
    final HttpServer client = new HttpServer();
    client.postJson("http://localhost:4224/jstd/proxy", proxyConfig);
    SocketConnector connector = new SocketConnector();
    connector.setPort(8888);
    org.mortbay.jetty.Server dummy = new org.mortbay.jetty.Server();
    dummy.addConnector(connector);
    Context context = new Context(dummy, "/", Context.SESSIONS);
    DummyServlet servlet = new DummyServlet();
    context.addServlet(new ServletHolder(servlet), "/");
    dummy.start();
    final PrintStream out = System.out;
    final int N = 8000;
    Thread[] threads = new Thread[2*N];
    final AtomicInteger a = new AtomicInteger(0);
    for (int i = 0; i < 2*N; ++i) {
      final int j = i;
      threads[i] = new Thread() {
        @Override public void run() {
          try {
            if (j % 2 == 0) {
              client.postJson("http://localhost:4224/asdf", new JsonArray());
            } else {
              HttpURLConnection connection = (HttpURLConnection)
                  new URL("http://localhost:4224/asdf").openConnection();
              // TODO(rdionne): Add Content-Type to prevent failure case after
              // proxy is rewritten.
              //connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
              connection.setRequestMethod("GET");
              read(connection.getInputStream());
            }
            a.incrementAndGet();
          } catch (Exception e) {
            e.printStackTrace(out);
            throw new RuntimeException(e);
          }
        }
      };
      threads[i].start();
    }
    for (int i = 0; i < 2*N; ++i) {
      threads[i].join();
    }
    assertEquals(2*N + " " + N + " " + N,
        a.intValue() + " " + servlet.gets.intValue() + " " + servlet.posts.intValue());
    server.stop();
  }

  private final class TestListener implements ServerListener {
    public boolean stopped;
    public boolean started;
    private BrowserInfo panicked;
    private BrowserInfo captured;

    public void serverStopped() {
      stopped = true;
    }

    public void serverStarted() {
      started = true;
    }

    public void browserPanicked(BrowserInfo info) {
      panicked = info;
    }

    public void browserCaptured(BrowserInfo info) {
      captured = info;
    }
  }

  private static final class DummyServlet extends HttpServlet {

    public final AtomicInteger gets = new AtomicInteger(0);
    public final AtomicInteger posts = new AtomicInteger(0);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
      gets.incrementAndGet();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
      posts.incrementAndGet();
    }
  }
}
