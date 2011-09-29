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
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.hooks.FileInfoScheme;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.server.JstdTestCaseStore;
import com.google.jstestdriver.util.HtmlWriter;

import junit.framework.TestCase;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Cory Smith (corbinrsmith@gmail.com)
 */
public class RunnerPageTest extends TestCase {
  public void testRenderWithPrefix() throws Exception {
    new PrefixTester().testPrefixes(new RunnerPage(new TestFileUtil(new JstdTestCaseStore(),
        new NullPathPrefix(), Collections.<FileInfoScheme>emptySet(), new Gson())));
  }

  public void testWriteResources() throws IOException {
    Map<String, FileInfo> files = Maps.newHashMap();
    FileInfo fileInfo = new FileInfo("foo.js", -1, -1, false, false, "", "foo.js");
    NullPathPrefix prefix = new NullPathPrefix();
    Set<FileInfoScheme> schemes = Collections.<FileInfoScheme>emptySet();
    JstdTestCaseStore store = new JstdTestCaseStore();
    String id = "foo";
    store.addCase(new JstdTestCase(Collections.<FileInfo>emptyList(), Lists.newArrayList(fileInfo),
        Collections.<FileInfo>emptyList(), id));
    RunnerPage page = new RunnerPage(new TestFileUtil(store, prefix, schemes, new Gson()));
    
    Map<String, String> properties = Maps.newHashMap();
    properties.put(SlavePageRequest.TESTCASE_ID, id);
    SlavePageRequest request =
        new SlavePageRequest(properties, null, prefix, null);
    CharArrayWriter writer = new CharArrayWriter();
    final HtmlWriter htmlWriter = new HtmlWriter(writer, prefix);
    page.render(htmlWriter, request);
    String html = writer.toString();
    String jsonFileInfo = new Gson().toJson(fileInfo.toFileSource(prefix, schemes));

    assertTrue(jsonFileInfo + " is not found in " + html, html.contains(jsonFileInfo));
  }
}
