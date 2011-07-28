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
import java.util.Observer;
import java.util.Set;
import java.util.Timer;

import org.mortbay.component.LifeCycle;
import org.mortbay.component.LifeCycle.Listener;
import org.mortbay.jetty.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.google.jstestdriver.browser.BrowserCaptureEvent;
import com.google.jstestdriver.browser.BrowserReaper;
import com.google.jstestdriver.hooks.FileInfoScheme;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.server.JettyModule;
import com.google.jstestdriver.server.handlers.JstdHandlersModule;
import com.google.jstestdriver.server.proxy.ProxyBehavior;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class JsTestDriverServerImpl implements JsTestDriverServer, Observer {

  private static final Logger logger = LoggerFactory.getLogger(JsTestDriverServerImpl.class);

  private Server server;

  private final int port;
  private final int sslPort;
  private final CapturedBrowsers capturedBrowsers;
  private final FilesCache filesCache;
  private final long browserTimeout;

  private Timer timer;

  private final HandlerPathPrefix handlerPrefix;
  private final ProxyBehavior proxyHostHeaderMode;

  private final Set<ServerListener> listeners;

  private final Set<FileInfoScheme> schemes;

  @Inject
  public JsTestDriverServerImpl(@Assisted("port") int port,
                                @Assisted("sslPort") int sslPort,
                                @Assisted FilesCache preloadedFilesCache,
                                CapturedBrowsers capturedBrowsers,
                                @Named("browserTimeout") long browserTimeout,
                                @Named("serverHandlerPrefix") HandlerPathPrefix handlerPrefix,
                                @Named("proxyHostHeaderMode") ProxyBehavior proxyHostHeaderMode,
                                Set<ServerListener> listeners,
                                Set<FileInfoScheme> schemes) {
    this.port = port;
    this.sslPort = sslPort;
    this.capturedBrowsers = capturedBrowsers;
    this.filesCache = preloadedFilesCache;
    this.browserTimeout = browserTimeout;
    this.handlerPrefix = handlerPrefix;
    this.proxyHostHeaderMode = proxyHostHeaderMode;
    this.listeners = listeners;
    this.schemes = schemes;
    initServer();
  }

  private void initServer() {
    if (server != null) {
      logger.warn("Attempt to start a started server");
    } else {
      // TODO(corysmith): move this to the normal guice injection scope.
      capturedBrowsers.deleteObserver(this);
      capturedBrowsers.addObserver(this);
      server = Guice.createInjector(
          new JettyModule(port, sslPort, handlerPrefix),
          new JstdHandlersModule(capturedBrowsers,
                                 filesCache,
                                 browserTimeout,
                                 handlerPrefix,
                                 proxyHostHeaderMode,
                                 schemes)).getInstance(Server.class);
      server.addLifeCycleListener(new JettyLifeCycleLogger());
    }
  }

  @Override
  public void start() {
    try {
      initServer();
      // TODO(corysmith): Move this to the constructor when we are injecting
      // everything.
      timer = new Timer(true);
      timer.schedule(new BrowserReaper(capturedBrowsers), browserTimeout * 2, browserTimeout * 2);

      server.start();
      logger.info("Started the JsTD server on {}", port);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stop() {
    try {
      timer.cancel();
      if (server != null) {
        server.stop();
        server.join();
        server = null;
      }

      logger.debug("Stopped the server.");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Separate function for synchronization and thread handling. */
  private void notifyListeners(ServerNotification<ServerListener> notification) {
    for (ServerListener listener : listeners) {
      notification.notify(listener);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.jstestdriver.JsTestDriverServer#isHealthy()
   */
  @Override
  public boolean isHealthy() {
    String url = "http://127.0.0.1:" + port + handlerPrefix.prefixPath("/hello");
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

  private final class JettyLifeCycleLogger implements Listener {
    @Override
    public void lifeCycleStopping(LifeCycle arg0) {
      logger.debug("Server stopping");
    }

    @Override
    public void lifeCycleStopped(LifeCycle arg0) {
      notifyListeners(STOPPED_NOTIFICATION);
      logger.debug("Server stopped");
    }

    @Override
    public void lifeCycleStarting(LifeCycle arg0) {
      logger.debug("Server starting");
    }

    @Override
    public void lifeCycleStarted(LifeCycle arg0) {
      notifyListeners(STARTED_NOTIFICATION);
      logger.debug("Server started");
    }

    @Override
    public void lifeCycleFailure(LifeCycle arg0, Throwable arg1) {
      logger.warn("Server failed", arg1);
    }
  }

  /**
   * Receives updates from the CapturedBrowsers.
   */
  @Override
  public void update(Observable o, Object arg) {
    // TODO(corysmith): Cleanup browser capture event.
    BrowserCaptureEvent event = (BrowserCaptureEvent) arg;
    final BrowserInfo info = event.getBrowser().getBrowserInfo();
    switch (event.event) {
      case CONNECTED:
        notifyListeners(new BrowserCaptureNotification(info));
        break;
      case DISCONNECTED:
        notifyListeners(new BrowserPanickedNotification(info));
        break;
    }
  }


  private static final class BrowserPanickedNotification implements ServerNotification<
      ServerListener> {

    private final BrowserInfo info;

    private BrowserPanickedNotification(BrowserInfo info) {
      this.info = info;
    }

    @Override
    public void notify(ServerListener listener) {
      listener.browserPanicked(info);
    }
  }

  private static final class BrowserCaptureNotification implements ServerNotification<
      ServerListener> {

    private final BrowserInfo info;

    private BrowserCaptureNotification(BrowserInfo info) {
      this.info = info;
    }

    @Override
    public void notify(ServerListener listener) {
      listener.browserCaptured(info);
    }
  }

  private static final ServerNotification<ServerListener> STARTED_NOTIFICATION =
      new ServerNotification<ServerListener>() {
        @Override
        public void notify(ServerListener listener) {
          listener.serverStarted();
        }
      };

  private static final ServerNotification<ServerListener> STOPPED_NOTIFICATION =
      new ServerNotification<ServerListener>() {
        @Override
        public void notify(ServerListener listener) {
          listener.serverStopped();
        }
      };

  /** Internal server notification. */
  private static interface ServerNotification<T> {
    public void notify(ServerListener listener);
  }
}
