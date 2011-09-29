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
package com.google.jstestdriver.plugins.testisolation;

import com.google.common.collect.Lists;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.hooks.JstdTestCaseProcessor;
import com.google.jstestdriver.model.JstdTestCase;

import java.util.Iterator;
import java.util.List;

/**
 * A test case processor for test isolation. 
 * 
 * Puts each test file into its own {@link JstdTestCase}.
 * 
 * @author Andrew Trenk
 */
public class IsolationTestCaseProcessor implements JstdTestCaseProcessor {

  @Override
  public List<JstdTestCase> process(Iterator<JstdTestCase> testCasesIterator) {
    List<JstdTestCase> isolatedTestCases = Lists.newArrayList();    
    List<JstdTestCase> testCases = Lists.newArrayList(testCasesIterator);
    
    // Add each test file of each JstdTestCase into its own JstdTestCase
    for (JstdTestCase jstdTestCase : testCases) {
      List<FileInfo> tests = jstdTestCase.getTests();

      for (FileInfo test : tests) {
        JstdTestCase testCase = new JstdTestCase(
            jstdTestCase.getDependencies(), Lists.newArrayList(test), jstdTestCase.getPlugins(), test.getDisplayPath().replace("/", "_"));
        isolatedTestCases.add(testCase);
      }
    }

    return isolatedTestCases;
  }  
}