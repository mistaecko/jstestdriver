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

goog.provide('jstestdriver.TestRunFilter');

goog.require('jstestdriver');

/**
 * @constructor
 */
jstestdriver.TestRunFilter = function(testCaseInfo) {
  this.testCaseInfo_ = testCaseInfo;
};


jstestdriver.TestRunFilter.prototype.getDefaultTestRunConfiguration = function() {
  return this.createTestRunConfiguration_(this.testCaseInfo_.getTestNames());
};


/**
 * Includes and excludes tests based on the given expressions. Expressions are
 * of the form:
 *
 * Expr:
 *   "all" | RegExp | -RegExp
 *
 * RegExp:
 *   A JavaScript regular expression without the quoting characters.
 *
 * @param expressions {Array.<string>} The expression strings.
 */
jstestdriver.TestRunFilter.prototype.getTestRunConfigurationFor = function(expressions) {
  var positiveExpressions = this.filter_(expressions, this.regexMatcher_(/^[^-].*/));
  if (positiveExpressions.length < 1) {
    positiveExpressions.push('all');
  }
  var negativeExpressions = this.filter_(expressions, this.regexMatcher_(/^-.*/));
  var testMethodMap = this.buildTestMethodMap_();
  var excludedTestIds = this.getExcludedTestIds_(testMethodMap, negativeExpressions);
  var matchedTests = this.getMatchedTests_(testMethodMap, positiveExpressions, excludedTestIds);
  return matchedTests.length > 0 ? this.createTestRunConfiguration_(matchedTests) : null;
};


jstestdriver.TestRunFilter.prototype.createTestRunConfiguration_ = function(tests) {
  return new jstestdriver.TestRunConfiguration(this.testCaseInfo_, tests);
};


/**
 * @param regex {RegExp} The regular expression.
 * @return {function(string): boolean} A function that tests the given RegExp
 *     against the function's expression argument.
 * @private
 */
jstestdriver.TestRunFilter.prototype.regexMatcher_ = function(regex) {
  return function(expression) {
    return regex.test(expression);
  };
};


/**
 * @return {Object.<string, string>} A map from test method id to test method
 *     name, where a test method id is of the form TestCaseName#testMethodName.
 * @private
 */
jstestdriver.TestRunFilter.prototype.buildTestMethodMap_ = function() {
  var testMethodMap = {};
  var testMethods = this.testCaseInfo_.getTestNames();
  var testMethodsLength = testMethods.length;
  for (var i = 0; i < testMethodsLength; ++i) {
    var methodName = testMethods[i];
    if (this.isTestMethod_(methodName)) {
      testMethodMap[this.buildTestMethodId_(methodName)] = methodName;
    }
  }
  return testMethodMap;
};


/**
 * @param methodName {string} A name of a method of the test class.
 * @return {boolean} True if the method name begins with 'test'.
 * @private
 */
jstestdriver.TestRunFilter.prototype.isTestMethod_ = function(methodName) {
  return /^test.*/.test(methodName);
};


/**
 * @param testMethod {string} The name of the test method.
 * @return {string} A test method id which is of the form
 *     TestCaseName#testMethodName.
 * @private
 */
jstestdriver.TestRunFilter.prototype.buildTestMethodId_ = function(testMethod) {
  return this.testCaseInfo_.getTestCaseName() + '#' + testMethod;
};


/**
 * @param expressions {Array.<string>} The expression strings.
 * @param condition {function(string): boolean} A condition that applies to the
 *     expression strings.
 * @return {Array.<string>} Any expression strings for which the condition holds.
 * @private
 */
jstestdriver.TestRunFilter.prototype.filter_ = function(expressions, condition) {
  var result = [];
  for (var i = 0; i < expressions.length; ++i) {
    if (condition(expressions[i])) {
      result.push(expressions[i]);
    }
  }
  return result;
};


/**
 * @param testMethodMap {Object.<string, string>} A map from test method id to
 *     test method name.
 * @param negativeExpressions {Array.<string>} The negative expression strings.
 * @return {Object.<string, boolean>} A map from test method id to boolean that
 *     signals whether a test method should be excluded from this test run.
 * @private
 */
jstestdriver.TestRunFilter.prototype.getExcludedTestIds_ = function(
    testMethodMap, negativeExpressions) {
  var excludedTestIds = {};
  for (var i = 0; i < negativeExpressions.length; ++i) {
    var expr = negativeExpressions[i].substring(1);
    var pattern = new RegExp(expr);
    for (var testMethodId in testMethodMap) {
      if (pattern.test(testMethodId)) {
        excludedTestIds[testMethodId] = true;
      }
    }
  }
  return excludedTestIds;
};

/**
 * @param testMethodMap {Object.<string, string>} A map from test method id to
 *     test method name.
 * @param positiveExpressions {Array.<string>} The positive expression strings.
 * @param excludedTestIds {Object.<string, boolean>} A map from test method id to
 *     boolean that signals whether a test method should be excluded from this
 *     test run.
 * @return {Array.<string>} A list of test method names for test methods that
 *     should be run.
 * @private
 */
jstestdriver.TestRunFilter.prototype.getMatchedTests_ = function(
    testMethodMap, positiveExpressions, excludedTestIds) {
  var matchedTests = [];
  for (var i = 0; i < positiveExpressions.length; i++) {
    var expr = positiveExpressions[i];

    if (expr == 'all') {
      expr = '.*';
    }

    var pattern = new RegExp(expr);

    for (var testMethodId in testMethodMap) {
      if (pattern.test(testMethodId) && !excludedTestIds[testMethodId]) {
        matchedTests.push(testMethodMap[testMethodId]);
      }
    }
  }
  return matchedTests;
};
