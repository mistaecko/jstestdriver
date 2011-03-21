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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Timer;

import org.mortbay.component.LifeCycle;
import org.mortbay.component.LifeCycle.Listener;
import org.mortbay.jetty.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.jstestdriver.browser.BrowserReaper;
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.server.JettyModule;
import com.google.jstestdriver.server.handlers.JstdHandlersModule;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class JsTestDriverServerImpl extends Observable implements JsTestDriverServer {

  private static final Logger logger =
      LoggerFactory.getLogger(JsTestDriverServerImpl.class);

  private Server server;

  private final int port;
  private final CapturedBrowsers capturedBrowsers;
  private final FilesCache filesCache;
  private final long browserTimeout;

  private Timer timer;

  private final HandlerPathPrefix handlerPrefix;

  public JsTestDriverServerImpl(int port,
                            CapturedBrowsers capturedBrowsers,
                            FilesCache preloadedFilesCache,
                            long browserTimeout,
                            HandlerPathPrefix handlerPrefix) {
    this.port = port;
    this.capturedBrowsers = capturedBrowsers;
    this.filesCache = preloadedFilesCache;
    this.browserTimeout = browserTimeout;
    this.handlerPrefix = handlerPrefix;
    initServer();
  }

  private void initServer() {
    if (server != null) {
      logger.warn("Attempt to start a started server");
    } else {
      // TODO(corysmith): move this to the normal guice injection scope.
      server = Guice.createInjector(
          new JettyModule(port, handlerPrefix),
          new JstdHandlersModule(
              capturedBrowsers,
              filesCache,
              browserTimeout,
              handlerPrefix)).getInstance(Server.class);
      server.addLifeCycleListener(new JettyLifeCycleLogger());
    }
  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.JsTestDriverServer#start()
   */
  public void start() {
    try {
      initServer();
      // TODO(corysmith): Move this to the constructor when we are injecting everything.
      timer = new Timer(true);
      timer.schedule(new BrowserReaper(capturedBrowsers), browserTimeout * 2, browserTimeout * 2);

      server.start();

      setChanged();
      notifyObservers(Event.STARTED);
      logger.info("Started the JsTD server on {}", port);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.JsTestDriverServer#stop()
   */
  public void stop() {
    try {
      timer.cancel();
      if (server != null) {
        server.stop();
        server.join();
        server = null;
      }
      setChanged();
      notifyObservers(Event.STOPPED);
      logger.debug("Stopped the server.");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.JsTestDriverServer#isHealthy()
   */
  public boolean isHealthy() {
    String url = "http://localhost:" + port + handlerPrefix.prefixPath("/hello");
    HttpURLConnection connection;
    try {
      connection = (HttpURLConnection) new URL(url).openConnection();
      connection.connect();
      int responseCode = connection.getResponseCode();
      if (responseCode == 200) {
        return true;
      }
      logger.warn("Bad response code {} from server: {}", responseCode, connection.getContent());
      return false;
    } catch (MalformedURLException e) {
      logger.warn("Bad url {}", e);
    } catch (IOException e) {
      logger.warn("Server not ready.", e);
    }
    return false;
  }

  private static final class JettyLifeCycleLogger implements Listener {
    public void lifeCycleStopping(LifeCycle arg0) {
      logger.debug("Server stopping");
    }

    public void lifeCycleStopped(LifeCycle arg0) {
      logger.debug("Server stopped");
    }

    public void lifeCycleStarting(LifeCycle arg0) {
      logger.debug("Server starting");
    }

    public void lifeCycleStarted(LifeCycle arg0) {
      logger.debug("Server started");
    }

    public void lifeCycleFailure(LifeCycle arg0, Throwable arg1) {
      logger.warn("Server failed", arg1);
    }
  }
}
