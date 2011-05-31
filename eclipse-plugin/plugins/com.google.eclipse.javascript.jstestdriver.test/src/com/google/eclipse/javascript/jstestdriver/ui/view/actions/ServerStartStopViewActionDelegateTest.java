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

import com.google.eclipse.javascript.jstestdriver.ui.Icons;
import com.google.eclipse.javascript.jstestdriver.ui.view.PortSupplier;
import com.google.eclipse.javascript.jstestdriver.ui.view.ServerController;

import junit.framework.TestCase;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

public class ServerStartStopViewActionDelegateTest extends TestCase {

  protected boolean startServerIconCalled = false;
  protected boolean stopServerIconCalled = false;

  public void testClickingOnActionTogglesIconAndTextBetweenStartStop() {
    Icons icons = new Icons() {

      @Override
      public ImageDescriptor startServerIcon() {
        startServerIconCalled = true;
        stopServerIconCalled = false;
        return null;
      }

      @Override
      public ImageDescriptor stopServerIcon() {
        startServerIconCalled = false;
        stopServerIconCalled = true;
        return null;
      }
    };
    
    
    ServerStartStopViewActionDelegate delegate = new ServerStartStopViewActionDelegate(
        icons, ServerController.getInstanceForTest(new PortSupplier() {
          @Override
          public int getPort() {
            return 42242;
          }
        }));
    Action action = new Action() {
    };

    delegate.run(action);

    assertFalse("Stop icon not called", stopServerIconCalled);
    assertTrue("Start icon called", startServerIconCalled);
    assertEquals("Start Server", action.getText());

    delegate.run(action);

    assertEquals("Stop Server", action.getText());
    assertFalse("Start icon not called", startServerIconCalled);
    assertTrue("Stop icon called", stopServerIconCalled);
  }

}
