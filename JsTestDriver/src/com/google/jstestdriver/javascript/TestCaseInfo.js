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

goog.provide('jstestdriver.TestCaseInfo');

goog.require('jstestdriver');
goog.require('jstestdriver.TestRunFilter');

/**
 * @param testCaseName
 * @param template
 * @param opt_type
 * @constructor
 */
jstestdriver.TestCaseInfo = function(testCaseName, template, opt_type) {
  this.testCaseName_ = testCaseName;
  this.template_ = template;
  this.type_ = opt_type || jstestdriver.TestCaseInfo.DEFAULT_TYPE;
};


jstestdriver.TestCaseInfo.DEFAULT_TYPE = 'default';


jstestdriver.TestCaseInfo.ASYNC_TYPE = 'async';


jstestdriver.TestCaseInfo.prototype.getType = function() {
  return this.type_;
};


jstestdriver.TestCaseInfo.prototype.getTestCaseName = function() {
  return this.testCaseName_;
};


jstestdriver.TestCaseInfo.prototype.getTemplate = function() {
  return this.template_;
};


jstestdriver.TestCaseInfo.prototype.getTestNames = function() {
  var testNames = [];

  for (var property in this.template_.prototype) {
    if (property.indexOf('test') == 0) {
      testNames.push(property);
    }
  }
  return testNames;
};


jstestdriver.TestCaseInfo.prototype.getDefaultTestRunConfiguration = function() {
  return new jstestdriver.TestRunFilter(this).getDefaultTestRunConfiguration();
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
jstestdriver.TestCaseInfo.prototype.getTestRunConfigurationFor = function(expressions) {
  return new jstestdriver.TestRunFilter(this).getTestRunConfigurationFor(expressions);
};


jstestdriver.TestCaseInfo.prototype.equals = function(obj) {
  return obj && typeof obj.getTestCaseName != 'undefined'
      && obj.getTestCaseName() == this.testCaseName_;
};


jstestdriver.TestCaseInfo.prototype.toString = function() {
  return "TestCaseInfo(" +
    this.testCaseName_ +
    "," + this.template_ +
    "," + this.type_ + ")";
};
