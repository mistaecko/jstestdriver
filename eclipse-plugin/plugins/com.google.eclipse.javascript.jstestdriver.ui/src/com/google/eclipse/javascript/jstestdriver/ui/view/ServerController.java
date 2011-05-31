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

package com.google.eclipse.javascript.jstestdriver.ui.view;

import com.google.eclipse.javascript.jstestdriver.core.Server;
import com.google.eclipse.javascript.jstestdriver.core.model.JstdServerListener;
import com.google.eclipse.javascript.jstestdriver.ui.Activator;
import com.google.eclipse.javascript.jstestdriver.ui.prefs.WorkbenchPreferencePage;

/**
 * Controls the server. There can be only one!
 * @author shyamseshadri@gmail.com (Shyam Seshadri)
 *
 */
public class ServerController {
  private static ServerController instance = null;
  private final JstdServerListener listener;
  private final PortSupplier portSupplier;

  public static synchronized ServerController getInstance() {
    if (instance == null) {
      instance = new ServerController(new JstdServerListener(), new PreferenceStorePortSupplier());
    }
    return instance;
  }
  
  public static synchronized ServerController getInstanceForTest(PortSupplier supplier) {
    if (instance == null) {
      instance = new ServerController(new JstdServerListener(), supplier);
    }
    return instance;
  }

  ServerController(JstdServerListener listener, PortSupplier portSupplier) {
    this.listener = listener;
    this.portSupplier = portSupplier;
  }

  public void startServer() {
    Server.getInstance(portSupplier.getPort(), listener).start();
  }

  public void stopServer() {
    Server.getInstance().stop();
  }
  
  public boolean isServerReady() {
    return listener.hasSlaves();
  }
  
  public boolean isServerStarted() {
    return listener.getServerState() == Server.State.STARTED;
  }

  public void connectObservers(ServerInfoPanel view) {
    listener.addObserver(view);
    listener.addObserver(view.getBrowserButtonPanel());
  }

  public static class PreferenceStorePortSupplier implements PortSupplier {
    @Override
    public int getPort() {
      int port = Activator.getDefault().getPreferenceStore().getInt(
        WorkbenchPreferencePage.PREFERRED_SERVER_PORT);
      return port;
    }
  }

  public void disconnectObservers(ServerInfoPanel view) {
    listener.deleteObserver(view);
    listener.deleteObserver(view.getBrowserButtonPanel());
  }
}
