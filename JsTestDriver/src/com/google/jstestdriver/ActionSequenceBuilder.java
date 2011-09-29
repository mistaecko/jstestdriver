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

import com.google.gson.JsonArray;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.jstestdriver.action.ConfigureGatewayAction;
import com.google.jstestdriver.action.UploadAction;
import com.google.jstestdriver.browser.BrowserActionExecutorAction;
import com.google.jstestdriver.output.PrintXmlTestResultsAction;
import com.google.jstestdriver.output.XmlPrinter;
import com.google.jstestdriver.server.JstdTestCaseStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * A builder for creating a sequence of {@link Action}s to be run by the
 * ActionRunner.
 *
 * @author corbinrsmith@gmail.com (Cory Smith)
 */
public class ActionSequenceBuilder {
  @SuppressWarnings("unused")
  private static final Logger logger =
      LoggerFactory.getLogger(ActionSequenceBuilder.class);

  private final ActionFactory actionFactory;
  private final CapturedBrowsers capturedBrowsers;
  private int localServerPort = -1;
  private int localServerSslPort = -1;
  private boolean reset;
  private List<String> tests = new LinkedList<String>();
  private List<String> dryRunFor = new LinkedList<String>();
  private List<String> commands = new LinkedList<String>();
  private XmlPrinter xmlPrinter;
  private final BrowserActionExecutorAction browserActionsRunner;
  private final FailureCheckerAction failureCheckerAction;
  private final UploadAction uploadAction;
  private final ConfigureGatewayAction.Factory gatewayActionFactory;
  private boolean raiseOnFailure = false;
  private JsonArray gatewayConfig;
  private final BrowserStartupAction browserStartup;

  private final JstdTestCaseStore testCaseStore;

  /**
   * Begins the building of an action sequence.
   */
  @Inject
  public ActionSequenceBuilder(ActionFactory actionFactory,
                               BrowserActionExecutorAction browserActionsRunner,
                               FailureCheckerAction failureCheckerAction,
                               UploadAction uploadAction,
                               CapturedBrowsers capturedBrowsers,
                               @Named("gateway") JsonArray gatewayConfig,
                               ConfigureGatewayAction.Factory gatewayActionFactory,
                               BrowserStartupAction browserStartup,
                               JstdTestCaseStore testCaseStore) {
    this.actionFactory = actionFactory;
    this.browserActionsRunner = browserActionsRunner;
    this.failureCheckerAction = failureCheckerAction;
    this.uploadAction = uploadAction;
    this.capturedBrowsers = capturedBrowsers;
    this.gatewayConfig = gatewayConfig;
    this.gatewayActionFactory = gatewayActionFactory;
    this.browserStartup = browserStartup;
    this.testCaseStore = testCaseStore;
  }

  /**
   * Wraps the current sequence of actions with the server start and stop
   * actions.
   */
  private void addServerActions(List<Action> actions, boolean leaveServerRunning) {
    ServerStartupAction serverStartupAction =
        actionFactory.getServerStartupAction(localServerPort, localServerSslPort,
            capturedBrowsers, testCaseStore);
    actions.add(0, serverStartupAction);
    // add browser startup here.
    if (!leaveServerRunning) {
      actions.add(new ServerShutdownAction(serverStartupAction));
    } else {
      actions.add(browserStartup);
    }
  }

  /**
   * Adds tests to the action sequence.
   *
   * @param tests
   *          The list of tests to be executed during this sequence.
   * @return the current builder.
   */
  public ActionSequenceBuilder addTests(List<String> tests) {
    this.tests.addAll(tests);
    return this;
  }

  /** Indicates that tests should be executed as a dry run. */
  public ActionSequenceBuilder asDryRunFor(List<String> dryRunFor) {
    this.dryRunFor.addAll(dryRunFor);
    return this;
  }

  /** Creates and returns a sequence of actions. */
  public List<Action> build() {
    List<Action> actions = new LinkedList<Action>();
    actions.add(gatewayActionFactory.create(gatewayConfig));
    actions.add(uploadAction);
    if (!leaveServerRunning()) {
      actions.add(browserActionsRunner);
    }
    if (xmlPrinter != null) {
      actions.add(new PrintXmlTestResultsAction(xmlPrinter));
    }

    // wrap the actions with the setup/teardown actions.
    if (needToStartServer()) {
      addServerActions(actions, leaveServerRunning());
    }
    if (!tests.isEmpty() && raiseOnFailure) {
      actions.add(failureCheckerAction);
    }
    return actions;
  }

  /** Method that derives whether or not to leave the server running. */
  private boolean leaveServerRunning() {
    return tests.isEmpty() && commands.isEmpty() && !reset && dryRunFor.isEmpty();
  }

  private boolean needToStartServer() {
    return localServerPort != -1;
  }

  /**
   * Indicates that the browser should be reset before executing the tests.
   */
  public ActionSequenceBuilder reset(boolean reset) {
    this.reset = reset;
    return this;
  }

  /**
   * Indicates that a local server should be started on the provided port.
   */
  public ActionSequenceBuilder withLocalServerPort(int localServerPort) {
    this.localServerPort = localServerPort;
    return this;
  }

  /**
   * Indicates that a local server should be started on the provided ssl port.
   */
  public ActionSequenceBuilder withLocalServerSslPort(int localServerSslPort) {
    this.localServerSslPort = localServerSslPort;
    return this;
  }

  /**
   * Adds a list commands to executed in the browser and the results to be
   * returned.
   */
  public ActionSequenceBuilder addCommands(List<String> commands) {
    this.commands.addAll(commands);
    return this;
  }

  public ActionSequenceBuilder printingResultsWhenFinished(XmlPrinter printer) {
    this.xmlPrinter = printer;
    return this;
  }

  /**
   * Throw an {@link FailureException} when there are no tests, a test fails, or
   * there are errors while loading a test.
   */
  public ActionSequenceBuilder raiseOnFailure() {
    raiseOnFailure = true;
    return this;
  }
}
