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
var TestRunFilterTest = jstestdriver.testCaseManager.TestCase('TestRunFilterTest');


TestRunFilterTest.prototype.testRegexMatcher = function() {
  var testRunFilter = new jstestdriver.TestRunFilter();
  var matcher = testRunFilter.regexMatcher_(/^asdf.*/);
  assertTrue(matcher('asdf1234'));
  assertFalse(matcher('abcdasdf1234'));
};


TestRunFilterTest.prototype.testBuildTestMethodMap = function() {
  var template = function() {};
  template.prototype.testSomething = function() {};
  template.prototype.testSomethingElse = function() {};
  var testCaseInfo = new jstestdriver.TestCaseInfo('asdf.Test', template);
  var testRunFilter = new jstestdriver.TestRunFilter(testCaseInfo);
  var testMethodMap = testRunFilter.buildTestMethodMap_();
  assertEquals('testSomething', testMethodMap['asdf.Test#testSomething']);
  assertEquals('testSomethingElse', testMethodMap['asdf.Test#testSomethingElse']);
};


TestRunFilterTest.prototype.testIsTestMethod = function() {
  var testRunFilter = new jstestdriver.TestRunFilter();
  assertFalse(testRunFilter.isTestMethod_('myMethod'));
  assertFalse(testRunFilter.isTestMethod_('mytestMethod'));
  assertTrue(testRunFilter.isTestMethod_('testMyTestMethod'));
};


TestRunFilterTest.prototype.testBuildTestMethodId = function() {
  var testCaseInfo = new jstestdriver.TestCaseInfo('asdf.Test');
  var testRunFilter = new jstestdriver.TestRunFilter(testCaseInfo);
  assertEquals('asdf.Test#myTestMethod', testRunFilter.buildTestMethodId_('myTestMethod'));
};


TestRunFilterTest.prototype.testFilter = function() {
  var testRunFilter = new jstestdriver.TestRunFilter();
  var source = [0, 1, 2, 3, 4, 5];
  var evens = testRunFilter.filter_(source, function(item) {return item % 2 == 0;});
  var odds = testRunFilter.filter_(source, function(item) {return item % 2 == 1;});
  assertEquals([0, 2, 4], evens);
  assertEquals([1, 3, 5], odds);
};
