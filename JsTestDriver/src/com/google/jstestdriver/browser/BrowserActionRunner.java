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
package com.google.jstestdriver.browser;

import com.google.common.collect.Lists;
import com.google.jstestdriver.BrowserAction;
import com.google.jstestdriver.JsTestDriverClient;
import com.google.jstestdriver.ResponseStream;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.util.StopWatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;


/**
 * Runs all actions on a specific browser.
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class BrowserActionRunner implements Callable<Collection<ResponseStream>> {
  private static final Logger logger = LoggerFactory.getLogger(BrowserActionRunner.class);

  private final String id;
  private final JsTestDriverClient client;
  private final List<BrowserAction> actions;

  private final StopWatch stopWatch;

  private final List<JstdTestCase> testCases;

  // TODO(corysmith): enable session manager.
  private final BrowserSessionManager sessionManager;

  public BrowserActionRunner(String id, JsTestDriverClient client, List<BrowserAction> actions,
      StopWatch stopWatch, List<JstdTestCase> testCases, BrowserSessionManager sessionManager) {
    this.id = id;
    this.client = client;
    this.actions = actions;
    this.stopWatch = stopWatch;
    this.testCases = testCases;
    this.sessionManager = sessionManager;
  }

  @Override
  public Collection<ResponseStream> call() {
    Collection<ResponseStream> responses = Lists.newArrayList();
    String sessionId = sessionManager.startSession(id);
    logger.debug("start session on {} with id {}", id, sessionId);
    for (JstdTestCase testCase : testCases) {
      for (BrowserAction action : actions) {
        stopWatch.start("run %s", action);
        logger.info("Running BrowserAction {} with {}", action, testCase);
        responses.add(action.run(id, client, null, testCase));
        stopWatch.stop("run %s", action);
      }
    }
    logger.debug("stopping session on {} with id {}", id, sessionId);
    sessionManager.stopSession(sessionId, id);
    return responses;
  }
}
