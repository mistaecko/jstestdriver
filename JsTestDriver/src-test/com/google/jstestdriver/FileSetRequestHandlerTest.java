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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.jstestdriver.SlaveBrowser.BrowserState;
import com.google.jstestdriver.browser.BrowserFileSet;
import com.google.jstestdriver.browser.BrowserIdStrategy;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.runner.RunnerType;
import com.google.jstestdriver.server.JstdTestCaseStore;
import com.google.jstestdriver.server.handlers.CaptureHandler;
import com.google.jstestdriver.servlet.fileset.BrowserFileCheck;
import com.google.jstestdriver.servlet.fileset.TestCaseUpload;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.Map;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class FileSetRequestHandlerTest extends TestCase {

  FileInfo createFile(String path, long timestamp) {
    return new FileInfo(path, timestamp, -1, false, false, null, path);
  }

  FileInfo createFile(FileInfo info, long timestamp) {
    return new FileInfo(info.getFilePath(), timestamp, -1, false, false, null,
        info.getDisplayPath());
  }

  public void testBrowserCheckAction() throws Exception {
    final String fileOne = "one.js";
    final String fileTwo = "two.js";
    final String fileThree = "three.js";


    final CapturedBrowsers browsers = new CapturedBrowsers(new BrowserIdStrategy(new MockTime(0)));
    final BrowserInfo browserInfo = new BrowserInfo();
    browserInfo.setName("firefox");
    final SlaveBrowser browser =
        new SlaveBrowser(new MockTime(0), "1", browserInfo, 100, null, CaptureHandler.QUIRKS,
            RunnerType.CLIENT, BrowserState.CAPTURED);
    browser.addFiles(Lists.newArrayList(createFile(fileOne, 1), createFile(fileTwo, 1),
        createFile(fileThree, 1)), new LoadedFiles());
    browsers.addSlave(browser);
    Gson gson = new Gson();
    final BrowserFileCheck browserFileCheck = new BrowserFileCheck(gson);

    JstdTestCase testCaseToRun =
        new JstdTestCase(Lists.newArrayList(createFile(fileOne, 3), createFile(fileTwo, 1)),
            Collections.<FileInfo>emptyList(), Collections.<FileInfo>emptyList(), "one");

    assertEquals(
        new BrowserFileSet(Lists.newArrayList(createFile(fileOne, 3)),
            Lists.newArrayList(createFile(fileThree, 3)), false),
            browserFileCheck.handle(browser, gson.toJson(testCaseToRun)));
  }

  public void testUploadFilesToServer() throws Exception {
    final String fileOne = "one.js";
    final String fileTwo = "two.js";

    Gson gson = new Gson();
    JstdTestCaseStore store = new JstdTestCaseStore();
    final TestCaseUpload serverFileUpload = new TestCaseUpload(store, gson);

    JstdTestCase jstdTestCase = new JstdTestCase(Lists.newArrayList(createFile(fileOne, 3), createFile(fileTwo, 1)),
        Collections.<FileInfo>emptyList(),
        Collections.<FileInfo>emptyList(), "one");
    serverFileUpload.handle(null, gson.toJson(Lists.<JstdTestCase>newArrayList(jstdTestCase)));

    assertEquals(jstdTestCase, store.getCase(jstdTestCase.getId()));
  }
}
