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
package com.google.jstestdriver.server.handlers.pages;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.server.JstdTestCaseStore;
import com.google.jstestdriver.util.HtmlWriter;

import java.io.IOException;
import java.util.List;

/**
 * Runner page 
 *
 * @author corbinrsmith@gmail.com (Cory Smith)
 */
public class StandaloneRunnerPage implements Page {
  private final TestFileUtil testFileUtil;
  private final JstdTestCaseStore store;

  @Inject
  StandaloneRunnerPage(TestFileUtil testFileUtil, JstdTestCaseStore store) {
    this.testFileUtil = testFileUtil;
    this.store = store;
  }
  
  @Override
  public void render(HtmlWriter writer, SlavePageRequest request) throws IOException {
    writer.startHead()
        .writeTitle("Console Runner")
        .writeExternalScript("/static/jstestdrivernamespace.js")
        .writeExternalScript("/static/lib/json2.js")
        .writeExternalScript("/static/lib/json_sans_eval.js")
        .writeExternalScript("/static/lib/jquery-min.js")
        .writeExternalScript("/static/standalonerunner.js")
        .writeScript(
            "jstestdriver.console = new jstestdriver.Console();" +
            "jstestdriver.runner = jstestdriver.config.createRunner(\n"+
            "  jstestdriver.config.createStandAloneExecutor);");
    List<JstdTestCase> casesToWrite = Lists.newArrayList();
    
    if (request.getParameter(SlavePageRequest.TESTCASE_ID) != null) {
      testFileUtil.writeTestFiles(writer, request.getParameter(SlavePageRequest.TESTCASE_ID));
    } else {
      for (JstdTestCase testCase : store.getCases()) {
        testFileUtil.writeTestFiles(writer, testCase.getId());
      }
    }

    writer.writeScript("jstestdriver.runner.listen(" +
        "jstestdriver.manualResourceTracker.getResults());");
    writer.finishHead()
      .startBody()
      .finishBody()
      .flush();
  }
}
