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
package com.google.eclipse.javascript.jstestdriver.ui.view.actions;

import com.google.eclipse.javascript.jstestdriver.core.Server;
import com.google.eclipse.javascript.jstestdriver.ui.Activator;
import com.google.eclipse.javascript.jstestdriver.ui.Icons;
import com.google.eclipse.javascript.jstestdriver.ui.view.JsTestDriverView;
import com.google.eclipse.javascript.jstestdriver.ui.view.ServerController;
import com.google.eclipse.javascript.jstestdriver.ui.view.ServerInfoPanel;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * ViewActionDelegate which responds to whenever the start or stop server button is pressed.
 *
 * @author shyamseshadri@gmail.com (Shyam Seshadri)
 */
public class ServerStartStopViewActionDelegate implements IViewActionDelegate {

  private final Icons icons;
  private ServerInfoPanel view;
  private final ServerController serverController;
  public ServerStartStopViewActionDelegate() {
    this(Activator.getDefault().getIcons(), ServerController.getInstance());
  }
  
  public ServerStartStopViewActionDelegate(Icons icons, ServerController serverController) {
    this.icons = icons;
    this.serverController = serverController;
  }

  @Override
  public void init(IViewPart view) {
    if (view instanceof JsTestDriverView) {
      this.view = ((JsTestDriverView) view).getServerInfoPanel();
    }
  }

  @Override
  public void run(IAction action) {
    // TODO(corysmith): replace Server with ServerController?
    if (!serverController.isServerStarted()) {
      try {
        serverController.startServer();
        setStartServerState(action);
      } catch (RuntimeException e) {
        e.printStackTrace();
        IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
          e.getMessage());
        ErrorDialog.openError(Display.getCurrent().getActiveShell(),
            "JS Test Driver", "JS Test Driver Error", status);
      }
    } else {
      ServerController.getInstance().stopServer();
      setStopServerState(action);
    }
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
  }

  private void setStopServerState(IAction action) {
    action.setText("Stop Server");
    action.setToolTipText("Stop Server");
    action.setImageDescriptor(icons.stopServerIcon());
    if (view != null) {
      view.setServerStartedAndWaitingForBrowsers(Server.getInstance().getCaptureUrl());
    }
  }

  private void setStartServerState(IAction action) {
    action.setText("Start Server");
    action.setToolTipText("Start Server");
    action.setImageDescriptor(icons.startServerIcon());
    if (view != null) {
      view.setServerStopped();
    }
  }
}