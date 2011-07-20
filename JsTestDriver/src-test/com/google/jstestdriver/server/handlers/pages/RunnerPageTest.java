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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.inject.internal.Maps;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FilesCache;
import com.google.jstestdriver.hooks.FileInfoScheme;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.util.HtmlWriter;

/**
 * @author Cory Smith (corbinrsmith@gmail.com)
 */
public class RunnerPageTest extends TestCase {
  public void testRenderWithPrefix() throws Exception {
    new PrefixTester().testPrefixes(
      new RunnerPage(
        new TestFileUtil(
          new FilesCache(Collections.<String, FileInfo>emptyMap()),
            new NullPathPrefix(), Collections
        .<FileInfoScheme>emptySet(), new Gson())));
  }

  public void testWriteResources() throws IOException {
    Map<String, FileInfo> files = Maps.newHashMap();
    FileInfo fileInfo = new FileInfo("foo.js", -1, -1, false, false, "", "foo.js");
    files.put("foo.js", fileInfo);
    NullPathPrefix prefix = new NullPathPrefix();
    Set<FileInfoScheme> schemes = Collections.<FileInfoScheme>emptySet();
    RunnerPage page =
        new RunnerPage(new TestFileUtil(new FilesCache(files), prefix, schemes, new Gson()));
    SlavePageRequest request =
        new SlavePageRequest(Collections.<String, String>emptyMap(), null, prefix, null);
    CharArrayWriter writer = new CharArrayWriter();
    final HtmlWriter htmlWriter = new HtmlWriter(writer, prefix);
    page.render(htmlWriter, request);
    String html = writer.toString();
    String jsonFileInfo = new Gson().toJson(fileInfo.toFileSource(prefix, schemes));

    assertTrue(jsonFileInfo + " is not found in " + html, html.contains(jsonFileInfo));
  }
}
