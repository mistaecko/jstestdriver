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

import com.google.common.collect.Sets;
import com.google.jstestdriver.JsTestDriverServer.Factory;
import com.google.jstestdriver.config.ExecutionType;
import com.google.jstestdriver.hooks.FileInfoScheme;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.model.RunData;
import com.google.jstestdriver.server.JstdTestCaseStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observer;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class ServerStartupAction implements ObservableAction {
  private static final Logger logger = LoggerFactory.getLogger(ServerStartupAction.class);
  private final int port;
  private final int sslPort;
  private final JstdTestCaseStore testCaseStore;
  private JsTestDriverServer server;
  private List<Observer> observerList = new LinkedList<Observer>();
  private final boolean preloadFiles;
  private final FileLoader fileLoader;
  private final Factory serverFactory;

  /**
   * Exists for backwards compatibility.
   * @deprecated Use other constructor.
   */
  @Deprecated
  public ServerStartupAction(int port, int sslPort, CapturedBrowsers capturedBrowsers,
      JstdTestCaseStore testCaseStore, URLTranslator urlTranslator, URLRewriter urlRewriter) {
    this(port, sslPort, testCaseStore, false, null, new DefaultServerFactory(capturedBrowsers,
        SlaveBrowser.TIMEOUT, new NullPathPrefix()));
  }

  public ServerStartupAction(
      int port,
      int sslPort,
      JstdTestCaseStore testCaseStore,
      boolean preloadFiles,
      FileLoader fileLoader,
      Factory serverFactory) {
    this.port = port;
    this.sslPort = sslPort;
    this.testCaseStore = testCaseStore;
    this.preloadFiles = preloadFiles;
    this.fileLoader = fileLoader;
    this.serverFactory = serverFactory;
  }

  public JsTestDriverServer getServer() {
    return server;
  }

  @Override
  public RunData run(RunData runData) {
    logger.info("Starting server on {}, ssl on {}", port, sslPort);

    if (preloadFiles) {
      logger.debug("Preloading files...");
      for (JstdTestCase testCase :runData.getTestCases()) {
        testCaseStore.addCase(testCase.applyDelta(testCase.createUnloadedDelta().loadFiles(fileLoader)));
      }
    }

    server = serverFactory.create(port, sslPort, testCaseStore);

    if (!observerList.isEmpty()) {
      throw new RuntimeException("Observers not supported during the transition to listeners.");
    }
    try {
      server.start();
      // try for 30 seconds.
      for (int i = 1; i < 31; i++) {
        if (server.isHealthy()) {
          return runData;
        }
        Thread.sleep(1000); // wait for the server to come up.
        if (i % 6 == 0) {
          logger.warn("Stopping unhealthy server and trying again.");
          server.stop(); // kill it off and try again
          server.start();
        }
      }
      throw new ServerStartupException("Server never healthy on " + port);
    } catch (Exception e) {
      throw new ServerStartupException("Error starting the server on " + port, e);
    }
  }

  @Override
  public void addObservers(List<Observer> observers) {
    observerList.addAll(observers);
  }

  private static final class DefaultServerFactory implements JsTestDriverServer.Factory {
    private final CapturedBrowsers capturedBrowsers;
    private final long timeout;
    private final NullPathPrefix nullPathPrefix;


    public DefaultServerFactory(CapturedBrowsers capturedBrowsers, long timeout,
        NullPathPrefix nullPathPrefix) {
      this.capturedBrowsers = capturedBrowsers;
      this.timeout = timeout;
      this.nullPathPrefix = nullPathPrefix;
    }

    @Override
    public JsTestDriverServer create(int port, int sslPort, JstdTestCaseStore testCaseStore) {
      return new JsTestDriverServerImpl(port, sslPort, testCaseStore, capturedBrowsers, timeout,
          nullPathPrefix, Sets.<ServerListener>newHashSet(), Collections.<FileInfoScheme>emptySet(),
          ExecutionType.INTERACTIVE);
    }
  }

  public static class ServerStartupException extends RuntimeException {

    private static final long serialVersionUID = -6070989642608849122L;

    public ServerStartupException(String msg) {
      super(msg);
    }

    public ServerStartupException(String msg, Exception e) {
      super(msg, e);
    }
  }
}
