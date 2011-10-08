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
import com.google.jstestdriver.hooks.FileInfoScheme;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.server.JstdTestCaseStore;

import junit.framework.TestCase;

import java.util.Collections;

/**
 * @author Cory Smith (corbinrsmith@gmail.com)
 */
public class BrowserControlledRunnerPageTest extends TestCase {
  public void testRenderWithPrefix() throws Exception {
    new PrefixTester().testPrefixes(new BrowserControlledRunnerPage(new TestFileUtil(new JstdTestCaseStore(),
      new NullPathPrefix(),
      Collections.<FileInfoScheme>emptySet(), new Gson())));
  }
}
