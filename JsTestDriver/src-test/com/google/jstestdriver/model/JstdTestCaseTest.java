// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.model;

import com.google.common.collect.Lists;
import com.google.jstestdriver.FileInfo;

import junit.framework.TestCase;

/**
 * @author corysmith@google.com (Cory Smith)
 * 
 */
public class JstdTestCaseTest extends TestCase {
  public void testApplyDelta() throws Exception {
    FileInfo fileInfoOld = new FileInfo("foo.js", 1, -1, false, false, "", "foo.js");
    FileInfo fileInfoNew = new FileInfo("foo.js", 2, -1, false, false, "", "foo.js");
    JstdTestCaseDelta delta =
        new JstdTestCaseDelta(Lists.<FileInfo>newArrayList(), Lists.<FileInfo>newArrayList(),
            Lists.newArrayList(fileInfoNew));
    JstdTestCase testCase =
        new JstdTestCase(Lists.<FileInfo>newArrayList(), Lists.<FileInfo>newArrayList(),
            Lists.<FileInfo>newArrayList(fileInfoOld), null);

    assertEquals(new JstdTestCase(Lists.<FileInfo>newArrayList(), Lists.<FileInfo>newArrayList(),
        Lists.newArrayList(fileInfoNew), null), testCase.applyDelta(delta));
  }

  public void testApplyDeltaWrongId() throws Exception {
    FileInfo fileInfoOld = new FileInfo("foo.js", 1, -1, false, false, "", "foo.js");
    FileInfo fileInfoNew = new FileInfo("foo.js", 2, -1, false, false, "", "foo.js");
    JstdTestCaseDelta delta =
      new JstdTestCaseDelta(Lists.<FileInfo>newArrayList(), Lists.<FileInfo>newArrayList(),
          Lists.newArrayList(fileInfoNew));
    JstdTestCase testCase =
      new JstdTestCase(Lists.<FileInfo>newArrayList(), Lists.<FileInfo>newArrayList(),
          Lists.<FileInfo>newArrayList(fileInfoOld), null);

    assertEquals(new JstdTestCase(Lists.<FileInfo>newArrayList(), Lists.<FileInfo>newArrayList(),
        Lists.newArrayList(fileInfoNew), null), testCase.applyDelta(delta));
  }

  public void testApplyDeltaMerge() throws Exception {
    FileInfo plugin = new FileInfo("plugin.js", -1, -1, false, false, "", "plugin.js");
    FileInfo dep = new FileInfo("dep.js", -1, -1, false, false, "", "dep.js");
    FileInfo test = new FileInfo("test.js", -1, -1, false, false, "", "test.js");
    FileInfo testOld = new FileInfo("testone.js", -1, -1, false, false, "", "testone.js");
    FileInfo testNew = new FileInfo("testone.js", 1, -1, false, false, "", "testone.js");
    JstdTestCaseDelta delta =
        new JstdTestCaseDelta(Lists.<FileInfo>newArrayList(),
            Lists.<FileInfo>newArrayList(testNew), Lists.<FileInfo>newArrayList());
    JstdTestCase testCase =
        new JstdTestCase(Lists.<FileInfo>newArrayList(dep), Lists.<FileInfo>newArrayList(test,
            testOld), Lists.<FileInfo>newArrayList(plugin), null);

    assertEquals(
        new JstdTestCase(Lists.<FileInfo>newArrayList(dep), Lists.<FileInfo>newArrayList(test,
            testNew), Lists.newArrayList(plugin), null), testCase.applyDelta(delta));
  }
}
