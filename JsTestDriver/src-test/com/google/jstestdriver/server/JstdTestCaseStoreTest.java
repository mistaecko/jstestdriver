// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.server;

import com.google.common.collect.Lists;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.model.JstdTestCaseDelta;

import junit.framework.TestCase;

/**
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class JstdTestCaseStoreTest extends TestCase {
  public void testAddTestCase() throws Exception {
    FileInfo one = new FileInfo("foo.js", 10, -1, false, false, null, "foo.js");
    JstdTestCaseStore store = new JstdTestCaseStore();
    JstdTestCase testCase =
        new JstdTestCase(Lists.<FileInfo>newArrayList(), Lists.<FileInfo>newArrayList(one),
            Lists.<FileInfo>newArrayList(), "1");
    store.addCase(testCase);
    assertTrue(store.getCases().contains(testCase));
  }

  public void testAddTestCasesWithSharedFiles() throws Exception {
    String testCaseIdOne = "1";
    String testCaseIdTwo = "2";
    String testCaseIdThree = "3";
    FileInfo one = new FileInfo("foo.js", 1, -1, false, false, null, "foo.js");
    FileInfo two = new FileInfo("foo.js", 1, -1, false, false, "foo2", "foo.js");
    JstdTestCaseStore store = new JstdTestCaseStore();
    JstdTestCase testCaseOne =
        new JstdTestCase(Lists.<FileInfo>newArrayList(), Lists.<FileInfo>newArrayList(one),
            Lists.<FileInfo>newArrayList(), testCaseIdOne);
    JstdTestCase testCaseTwo =
        new JstdTestCase(Lists.<FileInfo>newArrayList(), Lists.<FileInfo>newArrayList(two),
            Lists.<FileInfo>newArrayList(), testCaseIdTwo);
    JstdTestCase testCaseThree =
        new JstdTestCase(Lists.<FileInfo>newArrayList(), Lists.<FileInfo>newArrayList(one),
            Lists.<FileInfo>newArrayList(), testCaseIdThree);
    store.addCase(testCaseOne);
    assertTrue(store.getCases().contains(testCaseOne));
    store.addCase(testCaseTwo);
    assertTrue(store.getCases().contains(testCaseTwo));
    store.addCase(testCaseOne);
    store.addCase(testCaseThree);
    assertEquals("The data will be updated for test case one", two.getData(),
        store.getCase(testCaseIdOne).getTests().get(0).getData());
    assertEquals("The data will be updated for test case three", two.getData(),
        store.getCase(testCaseIdThree).getTests().get(0).getData());
  }
  
  public void testApplyDelta() throws Exception {
    String contents = "foo";
    String testCaseIdOne = "1";
    FileInfo one = new FileInfo("foo.js", 1, -1, false, false, null, "foo.js");
    JstdTestCaseStore store = new JstdTestCaseStore();
    JstdTestCase testCaseOne =
      new JstdTestCase(Lists.<FileInfo>newArrayList(), Lists.<FileInfo>newArrayList(one),
          Lists.<FileInfo>newArrayList(), testCaseIdOne);
    JstdTestCaseDelta unloadedDelta = store.addCase(testCaseOne);

    store.applyDelta(new JstdTestCaseDelta(
        Lists.<FileInfo>newArrayList(),
        Lists.<FileInfo>newArrayList(one.load(contents, 1)),
        Lists.<FileInfo>newArrayList()));

    assertEquals("The data will be updated for test case one", contents,
        store.getCase(testCaseIdOne).getTests().get(0).getData());
    
    String testCaseIdTwo = "2";
    JstdTestCase testCaseTwo =
      new JstdTestCase(Lists.<FileInfo>newArrayList(), Lists.<FileInfo>newArrayList(one),
          Lists.<FileInfo>newArrayList(), testCaseIdTwo);
    store.addCase(testCaseTwo);

    assertEquals("The data will be updated for test case two", contents,
        store.getCase(testCaseIdTwo).getTests().get(0).getData());
  }
}
