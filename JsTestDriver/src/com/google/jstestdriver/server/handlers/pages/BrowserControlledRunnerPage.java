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

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FileSource;
import com.google.jstestdriver.FilesCache;
import com.google.jstestdriver.hooks.FileInfoScheme;
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.server.JstdTestCaseStore;
import com.google.jstestdriver.util.HtmlWriter;

import java.io.IOException;
import java.util.Set;

/**
 * Runner page 
 *
 * @author corbinrsmith@gmail.com (Cory Smith)
 */
public class BrowserControlledRunnerPage implements Page {
  private final JstdTestCaseStore store;
  private final Gson gson = new Gson();
  private final Set<FileInfoScheme> schemes;
  private final HandlerPathPrefix prefix;

  @Inject
  BrowserControlledRunnerPage(JstdTestCaseStore store, HandlerPathPrefix prefix, Set<FileInfoScheme> schemes) {
    this.store = store;
    this.prefix = prefix;
    this.schemes = schemes;
  }
  public void render(HtmlWriter writer, SlavePageRequest request) throws IOException {
    writer.startHead()
        .writeTitle("Console Runner")
        .writeExternalScript("/static/jstestdrivernamespace.js")
        .writeExternalScript("/static/lib/json2.js")
        .writeExternalScript("/static/lib/json_sans_eval.js")
        .writeExternalScript("/static/lib/jquery-min.js")
        .writeExternalScript("/static/browser_controlled_runner.js")
        .writeStyleSheet("/static/bcr.css")
        .writeScript(
            "jstestdriver.console = new jstestdriver.Console();" +
            "jstestdriver.runner = jstestdriver.config.createRunner(\n"+
            "    jstestdriver.config.createVisualExecutor);");
    
    if (!store.getCases().isEmpty()) {
      JstdTestCase testCase = store.getCase(request.getParameter("test"));
      if (testCase == null && !store.getCases().isEmpty()) {
        testCase = store.getCases().iterator().next();
      }
      
      for (FileInfo file : testCase) {
        if (!file.canLoad()) {
          continue;
        }
        FileSource fileSource = file.toFileSource(prefix, schemes);
        writer.writeScript(String.format(
          "jstestdriver.manualResourceTracker.startResourceLoad('%s')",
          gson.toJson(fileSource)));
        if (fileSource.getFileSrc().endsWith(".css")) {
          writer.writeStyleSheet(fileSource.getFileSrc());
        } else {
          writer.writeExternalScript(fileSource.getFileSrc());
        }
        writer.writeScript("jstestdriver.manualResourceTracker.finishResourceLoad()");
      }

    }
    writer.writeScript("jstestdriver.reporter.addLoadedFileResults(" +
        "jstestdriver.manualResourceTracker.getResults());");

    writer.writeScript("jstestdriver.runner.listen(" +
        "jstestdriver.manualResourceTracker.getResults());");
    writer.finishHead()
      .startBody()
      .finishBody()
      .flush();
  }
}
