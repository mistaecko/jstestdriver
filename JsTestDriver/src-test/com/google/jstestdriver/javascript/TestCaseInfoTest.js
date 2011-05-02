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
var TestCaseInfoTest = jstestdriver.testCaseManager.TestCase('TestCaseInfoTest');


TestCaseInfoTest.prototype.testEqualsTestCasesInfo = function() {
  var testCaseInfo1 = new jstestdriver.TestCaseInfo('testCase', function() {});
  var testCaseInfo2 = new jstestdriver.TestCaseInfo('testCase', function() {});
  var testCaseInfo3 = new jstestdriver.TestCaseInfo('notTestCase', function() {});

  assertTrue(testCaseInfo1.equals(testCaseInfo2));
  assertFalse(testCaseInfo1.equals(testCaseInfo3));
};


TestCaseInfoTest.prototype.testGetCorrectTestNames = function() {
  var testCaseManager = new jstestdriver.TestCaseManager();
  var testCaseBuilder = new jstestdriver.TestCaseBuilder(testCaseManager);
  var testCaseClass = testCaseBuilder.TestCase('testCase');

  testCaseClass.prototype.testFoo = function() {};
  testCaseClass.prototype.testBar = function() {};
  testCaseClass.prototype.testFooBar = function() {};
  testCaseClass.prototype.testFuBar = function() {};
  var testCaseInfo = new jstestdriver.TestCaseInfo('testCase', testCaseClass);
  var testNames = testCaseInfo.getTestNames();

  assertEquals(4, testNames.length);
  assertEquals('testFoo', testNames[0]);
  assertEquals('testBar', testNames[1]);
  assertEquals('testFooBar', testNames[2]);
  assertEquals('testFuBar', testNames[3]);
};


TestCaseInfoTest.prototype.testDefaultTestRunConfiguration = function() {
  var testCaseManager = new jstestdriver.TestCaseManager();
  var testCaseBuilder = new jstestdriver.TestCaseBuilder(testCaseManager);
  var testCaseClass = testCaseBuilder.TestCase('testCase');

  testCaseClass.prototype.testFoo = function() {};
  testCaseClass.prototype.testBar = function() {};
  var testCaseInfo = new jstestdriver.TestCaseInfo('testCase', testCaseClass);
  var testRunConfiguration = testCaseInfo.getDefaultTestRunConfiguration();

  assertEquals(testCaseInfo.getTestNames(), testRunConfiguration.getTests());
  assertSame(testCaseInfo, testRunConfiguration.getTestCaseInfo());
};


TestCaseInfoTest.prototype.testGetTestRunConfigurationFor = function() {
  var testCaseManager = new jstestdriver.TestCaseManager();
  var testCaseManagerPlugin = new jstestdriver.plugins.TestCaseManagerPlugin();
  var testCaseBuilder = new jstestdriver.TestCaseBuilder(testCaseManager);
  var testCaseInfos = [];
  var testCaseClasses = [];
  testCaseClasses.push(testCaseBuilder.TestCase('apps.AppsTest'));
  testCaseInfos.push(new jstestdriver.TestCaseInfo('apps.AppsTest', testCaseClasses[0]));
  testCaseClasses.push(testCaseBuilder.TestCase('apps.sys.SysTest'));
  testCaseInfos.push(new jstestdriver.TestCaseInfo('apps.sys.SysTest', testCaseClasses[1]));
  testCaseClasses.push(testCaseBuilder.TestCase('util.UtilTest'));
  testCaseInfos.push(new jstestdriver.TestCaseInfo('util.UtilTest', testCaseClasses[2]));

  for (var i = 0; i < 3; ++i) {
    testCaseClasses[i].prototype.testFoo = function() {};
    testCaseClasses[i].prototype.testBar = function() {};
    testCaseClasses[i].prototype.testBaz = function() {};
  }

  var expressions = ['apps'];
  var runs = [];
  testCaseManagerPlugin.getTestRunsConfigurationFor(testCaseInfos, expressions, runs);

  assertEquals(2, runs.length);
  assertEquals(3, runs[0].getTests().length);
  assertEquals(3, runs[1].getTests().length);

  expressions = ['apps.sys'];
  runs = [];
  testCaseManagerPlugin.getTestRunsConfigurationFor(testCaseInfos, expressions, runs);

  assertEquals(1, runs.length);

  expressions = ['unknown'];
  runs = [];
  testCaseManagerPlugin.getTestRunsConfigurationFor(testCaseInfos, expressions, runs);

  assertEquals(0, runs.length);

  expressions = ['-.*'];
  runs = [];
  testCaseManagerPlugin.getTestRunsConfigurationFor(testCaseInfos, expressions, runs);

  assertEquals(0, runs.length);

  expressions = ['all', '-.*'];
  runs = [];
  testCaseManagerPlugin.getTestRunsConfigurationFor(testCaseInfos, expressions, runs);

  assertEquals(0, runs.length);

  expressions = ['util'];
  runs = [];
  testCaseManagerPlugin.getTestRunsConfigurationFor(testCaseInfos, expressions, runs);

  assertEquals(1, runs.length);

  expressions = ['apps', 'util'];
  runs = [];
  testCaseManagerPlugin.getTestRunsConfigurationFor(testCaseInfos, expressions, runs);

  assertEquals(3, runs.length);

  expressions = ['all'];
  runs = [];
  testCaseManagerPlugin.getTestRunsConfigurationFor(testCaseInfos, expressions, runs);

  assertEquals(3, runs.length);

  expressions = ['util.UtilTest#testFoo'];
  runs = [];
  testCaseManagerPlugin.getTestRunsConfigurationFor(testCaseInfos, expressions, runs);

  assertEquals(1, runs.length);
  assertEquals(1, runs[0].getTests().length);

  expressions = ['apps', '-apps.sys'];
  runs = [];
  testCaseManagerPlugin.getTestRunsConfigurationFor(testCaseInfos, expressions, runs);

  assertEquals(1, runs.length);

  expressions = ['all', '-util'];
  runs = [];
  testCaseManagerPlugin.getTestRunsConfigurationFor(testCaseInfos, expressions, runs);

  assertEquals(2, runs.length);

  expressions = ['#testFoo'];
  runs = [];
  testCaseManagerPlugin.getTestRunsConfigurationFor(testCaseInfos, expressions, runs);

  assertEquals(3, runs.length);
  assertEquals(1, runs[0].getTests().length);
  assertEquals(1, runs[1].getTests().length);
  assertEquals(1, runs[2].getTests().length);

  expressions = ['-#testFoo'];
  runs = [];
  testCaseManagerPlugin.getTestRunsConfigurationFor(testCaseInfos, expressions, runs);

  assertEquals(3, runs.length);
  assertEquals(2, runs[0].getTests().length);
  assertEquals(2, runs[1].getTests().length);
  assertEquals(2, runs[2].getTests().length);
};
