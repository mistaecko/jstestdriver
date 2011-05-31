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
package com.google.eclipse.javascript.jstestdriver.core;

import static java.lang.String.format;

import com.google.eclipse.javascript.jstestdriver.core.model.JstdServerListener;
import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.embedded.JsTestDriverBuilder;


/**
 * TODO: This should not use CaptureBrowsers and ServerStartupAction directly but go through the
 * IDEPluginActionBuilder
 *
 * Server responsible for starting a singleton instance of the JSTestDriver Server. Also has a
 * handle on the captured slave browsers.
 *
 * @author shyamseshadri@gmail.com (Shyam Seshadri)
 */
public class Server {
  
  public static enum State {
    STOPPED, STARTED
  }

  /**
   * URL Format of the server. Needs to be injected with an integer port number.
   */
  private static final String SERVER_URL_FORMAT = "http://127.0.0.1:%d";

  /**
   * URL Format of the browser capture url. Needs to be injected with a integer port number.
   */
  private static final String SERVER_CAPTURE_URL_FORMAT = "http://127.0.0.1:%d/capture";

  private boolean started = false;

  private static volatile Server instance;
  private final int port;

  private final JsTestDriver jstd;

  /**
   * Creates a Server at the given port and returns it the first time. Every call after the first
   * returns the instance created initially, ignoring the port value passed to it.
   *
   * @param port the integer port at which to create the server on.
   * @param jstdServerListener 
   * @return an initialized server.
   */
  public static Server getInstance(int port, JstdServerListener jstdServerListener) {
    if (instance == null || instance.getPort() != port) {
      synchronized (Server.class) {
        if (instance != null && instance.getPort() != port) {
          instance.stop();
        }
        if (instance == null) {
          JsTestDriver jstd = new JsTestDriverBuilder()
              .setDefaultConfiguration(new EclipseServerConfiguration())
              .addServerListener(jstdServerListener)
              .setPort(port).build();
          instance = new Server(jstd, port);
        }
      }
    }
    return instance;
  }

  private int getPort() {
    return port;
  }

  private Server(JsTestDriver jstd, int port) {
    this.jstd = jstd;
    this.port = port;
  }

  /**
   * Gets the singleton instance of the Server, {@code null} if not initialized yet.
   * @return the singleton instance of the server.
   */
  public static Server getInstance() {
    return instance;
  }

  /**
   * @return true if the server has been started
   */
  public synchronized boolean isStarted() {
    return started;
  }

  /**
   * Starts the JS Test Driver server
   */
  public synchronized void start() {
    if (!started) {
      jstd.startServer();
      started = true;
    }
  }

  public String getCaptureUrl() {
    return format(SERVER_CAPTURE_URL_FORMAT, port);
  }

  public String getServerUrl() {
    return format(SERVER_URL_FORMAT, port);
  }

  /**
   * Stops the JS Test Driver server
   */
  public synchronized void stop() {
    if (started) {
      jstd.stopServer();
      started = false;
    }
  }
}