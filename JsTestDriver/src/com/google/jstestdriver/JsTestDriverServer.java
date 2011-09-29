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

import com.google.inject.assistedinject.Assisted;
import com.google.jstestdriver.server.JstdTestCaseStore;


public interface JsTestDriverServer {

  public interface Factory {
    JsTestDriverServer create(@Assisted("port") int port,
                              @Assisted("sslPort") int sslPort,
                              JstdTestCaseStore testCaseStore);
  }

  void start();

  void stop();

  /**
   * Gets /hello to see if the server is active.
   */
  boolean isHealthy();

  public static class ServerEvent {
    
  }

  public enum Event {
    STARTED,
    STOPPED,
    BROWSER_CONNECTED,
    BROWSER_DISCONNECTED
  }
}
