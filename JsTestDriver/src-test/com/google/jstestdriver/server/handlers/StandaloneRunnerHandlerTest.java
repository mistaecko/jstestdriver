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
package com.google.jstestdriver.server.handlers;

import com.google.common.collect.Lists;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.Command;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FileSource;
import com.google.jstestdriver.FilesCache;
import com.google.jstestdriver.MockTime;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.SlaveBrowser.BrowserState;
import com.google.jstestdriver.SlaveResourceService;
import com.google.jstestdriver.browser.BrowserIdStrategy;
import com.google.jstestdriver.hooks.FileInfoScheme;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.runner.RunnerType;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class StandaloneRunnerHandlerTest extends TestCase {

  public void testCaptureAddFilesToLoadAndRun() throws Exception {
    List<FileInfo> fileList = Lists.newArrayList(
      new FileInfo("file1.js", 30, -1, false, false, "content1", "file1.js"),
      new FileInfo("file2.js", 5, -1, false, false, "content2", "file2.js"),
      new FileInfo("file3.js", 53, -1, false, false, "content3", "file3.js"),
      new FileInfo("file4.js", 1, -1, false, false, "content4", "file4.js")
      );
    List<FileSource> fileSources = Lists.newArrayList();
    final Map<String, FileInfo> files = new LinkedHashMap<String, FileInfo>();
    for (FileInfo file : fileList) {
      fileSources.add(file.toFileSource(new NullPathPrefix(), Collections.<FileInfoScheme>emptySet()));
      files.put(file.getDisplayPath(), file);
    }
    FilesCache cache = new FilesCache(files);
    CapturedBrowsers capturedBrowsers = new CapturedBrowsers(new BrowserIdStrategy(new MockTime(0)));
    BrowserInfo browserInfo = new BrowserInfo();
    browserInfo.setUploadSize(50);
    SlaveBrowser slaveBrowser =
        new SlaveBrowser(new MockTime(10), "1", browserInfo, 1200, null,
            CaptureHandler.QUIRKS, RunnerType.CLIENT, BrowserState.CAPTURED);
    capturedBrowsers.addSlave(slaveBrowser);
    StandaloneRunnerHandler handler =
        new StandaloneRunnerHandler(null, null, new SlaveResourceService(""),
            new ConcurrentHashMap<SlaveBrowser, Thread>(), null, null);
    handler.service(slaveBrowser);

    assertNotNull(slaveBrowser.peekCommand());
    Command cmd = slaveBrowser.dequeueCommand();
    assertNotNull(cmd);
    assertEquals("{\"command\":\"runAllTests\",\"parameters\":[\"false\",\"false\",\"0\"]}",
        cmd.getCommand());
  }
}
