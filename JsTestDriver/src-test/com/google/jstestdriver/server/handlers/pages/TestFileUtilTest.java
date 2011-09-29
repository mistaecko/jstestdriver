/*
 * Copyright 2011 Google Inc.
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
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.HttpFileInfoScheme;
import com.google.jstestdriver.hooks.FileInfoScheme;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.server.JstdTestCaseStore;
import com.google.jstestdriver.util.HtmlWriter;

import junit.framework.TestCase;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Cory Smith (corbinrsmith@gmail.com) 
 */
public class TestFileUtilTest extends TestCase {

  private static final String TESTCASE_ID = "foo";

  public void testWriteFileInfos() throws Exception {
    Set<FileInfoScheme> defaultSchemes = Sets.<FileInfoScheme>newHashSet(new HttpFileInfoScheme());

    TestFileUtil testFileUtil = new TestFileUtil(createFileCache(
      new FileInfo("/foo.js", 0, 0, false, false, null, "foo.js"),
      new FileInfo("/serveonly.js", 0, 0, false, true, null, "/serveonly.js"),
      new FileInfo("/bar.js", 0, 0, false, false, null, "bar.js"),
      new FileInfo("http://somehost/bar.js", 0, 0, false, false, null, "http://somehost/bar.js")
      ),
      new NullPathPrefix(), defaultSchemes, new Gson());

    final List<String> paths = Lists.newArrayList();

    testFileUtil.writeTestFiles(new HtmlWriter(new StringWriter(), new NullPathPrefix()) {
      @Override
      public HtmlWriter writeExternalScript(String path) {
        paths.add(path);
        return this;
      }
    }, TESTCASE_ID);
    assertEquals(Lists.newArrayList("/test/foo.js","/test/bar.js", "http://somehost/bar.js"), paths);
  }

  /**
   * If there is a FileInfo in the FileCache that cannot be loaded by script of link tag, the loading
   * should stop there.
   * @throws Exception
   */
  public void testEndWriteEarly() throws Exception {
    final String unhandledPath = "auth://foo@bar.com";

    Set<FileInfoScheme> schemes = Sets.<FileInfoScheme>newHashSet(new HttpFileInfoScheme(), new FileInfoScheme(){
      @Override
      public boolean matches(String path) {
        return unhandledPath.equals(path);
      }
    });

    TestFileUtil testFileUtil = new TestFileUtil(createFileCache(
      new FileInfo("/foo.js", 0, 0, false, false, null, "foo.js"),
      new FileInfo("/serveonly.js", 0, 0, false, true, null, "/serveonly.js"),
      new FileInfo("/bar.js", 0, 0, false, false, null, "bar.js"),
      new FileInfo(unhandledPath, 0, 0, false, false, null, unhandledPath),
      new FileInfo("http://somehost/bar.js", 0, 0, false, false, null, "http://somehost/bar.js")
      ),
      new NullPathPrefix(), schemes, new Gson());

    final List<String> paths = Lists.newArrayList();

    testFileUtil.writeTestFiles(new HtmlWriter(new StringWriter(), new NullPathPrefix()) {
      @Override
      public HtmlWriter writeExternalScript(String path) {
        paths.add(path);
        return this;
      }
    }, TESTCASE_ID);
    assertEquals(Lists.newArrayList("/test/foo.js","/test/bar.js"), paths);
  }

  private JstdTestCaseStore createFileCache(FileInfo... files) {
    JstdTestCaseStore store = new JstdTestCaseStore();
    store.addCase(new JstdTestCase(Lists.newArrayList(files), Collections.<FileInfo>emptyList(), Collections.<FileInfo>emptyList(), TESTCASE_ID));
    
    return store;
  }
  public void testBreakOnUnloadableWriteFileInfos() throws Exception {
    
  }
}
