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


/**
 * @fileoverview Defines the AsyncTestRunnerPlugin class, which executes
 * asynchronous test cases within JsTestDriver.
 *
 *     +----------------------------- more tests? ------------ nextTest() <--------------+
 *     |                                                                                 |
 *     v                                                                                 |
 * startSetUp() ---- execute ---> finishSetUp(errors)                                    |
 *                                     |                                                 |
 * startTestMethod() <--- no errors ---+---- errors ----+                                |
 *        |                                             |                                |
 *     execute                                          |                                |
 *        |                                             |                                |
 *        v                                             v                                |
 * finishTestMethod(errors) -- errors or no errors -> startTearDown() -- execute -> finishTearDown(errors)
 *
 * @author rdionne@google.com (Robert Dionne)
 */

goog.provide('jstestdriver.plugins.async.AsyncTestRunnerPlugin');

goog.require('jstestdriver');
goog.require('jstestdriver.setTimeout');
goog.require('jstestdriver.TestCaseInfo');
goog.require('jstestdriver.TestResult');
goog.require('jstestdriver.plugins.async.CallbackPool');
goog.require('jstestdriver.plugins.async.CallbackPoolArmor');
goog.require('jstestdriver.plugins.async.DeferredQueue');
goog.require('jstestdriver.plugins.async.DeferredQueueArmor');
goog.require('jstestdriver.plugins.async.TestStage');
goog.require('jstestdriver.plugins.async.TestStage.Builder');

/**
 * Constructs an AsyncTestRunnerPlugin.
 *
 * @param {Function} dateObj the date object constructor
 * @param {Function} clearBody a function to call to clear the document body.
 * @param {Function} toJson a function to call to convert an object to JSON.
 * @param {boolean} opt_pauseForHuman Whether to pause for debugging.
 * @param {Function} opt_setTimeout window.setTimeout replacement.
 * @param {Function} opt_queueConstructor a constructor for obtaining new
 *     DeferredQueues.
 * @param {Function} opt_armorConstructor a constructor for obtaining new
 *     DeferredQueueArmors.
 * @constructor
 */
jstestdriver.plugins.async.AsyncTestRunnerPlugin = function(dateObj, clearBody,
      toJson, opt_pauseForHuman, opt_setTimeout, opt_queueConstructor,
      opt_armorConstructor) {
  this.name = "AsyncTestRunnerPlugin";
  this.dateObj_ = dateObj;
  this.clearBody_ = clearBody;
  this.toJson_ = toJson;
  this.pauseForHuman_ = !!opt_pauseForHuman;
  this.setTimeout_ = opt_setTimeout || jstestdriver.setTimeout;
  this.queueConstructor_ = opt_queueConstructor || jstestdriver.plugins.async.DeferredQueue;
  this.armorConstructor_ = opt_armorConstructor || jstestdriver.plugins.async.DeferredQueueArmor;
  this.testRunConfiguration_ = null;
  this.testCaseInfo_ = null;
  this.onTestDone_ = null;
  this.onTestRunConfigurationComplete_ = null;
  this.testIndex_ = 0;
  this.testCase_ = null;
  this.testName_ = null;
  this.start_ = null;
  this.errors_ = null;
};

/**
 * Runs a test case.
 *
 * @param {jstestdriver.TestRunConfiguration} testRunConfiguration the test 
 *        case configuration
 * @param {function(jstestdriver.TestResult)} onTestDone the function to call to 
 *        report a test is complete
 * @param {function()=} opt_onTestRunConfigurationComplete the function to call 
 *        to report a test case is complete. A no-op will be used if this is
 *        not specified.
 */
jstestdriver.plugins.async.AsyncTestRunnerPlugin.prototype.runTestConfiguration = function(
    testRunConfiguration, onTestDone, opt_onTestRunConfigurationComplete) {
  if (testRunConfiguration.getTestCaseInfo().getType() == jstestdriver.TestCaseInfo.ASYNC_TYPE) {
    this.testRunConfiguration_ = testRunConfiguration;
    this.testCaseInfo_ = testRunConfiguration.getTestCaseInfo();
    this.onTestDone_ = onTestDone;
    this.onTestRunConfigurationComplete_ = opt_onTestRunConfigurationComplete ||
        function() {};
    this.testIndex_ = 0;
    this.nextTest();
    return true;
  }

  return false;
};

/**
 * Runs the next test in the current test case.
 */
jstestdriver.plugins.async.AsyncTestRunnerPlugin.prototype.nextTest = function() {
  this.start_ = new this.dateObj_().getTime();
  if (this.testIndex_ < this.testRunConfiguration_.getTests().length) {
    jstestdriver.expectedAssertCount = -1;
    jstestdriver.assertCount = 0;
    this.testCase_ = new (this.testCaseInfo_.getTemplate());
    this.testName_ = this.testRunConfiguration_.getTests()[this.testIndex_];
    this.errors_ = [];
    this.startSetUp();
  } else {
    this.testRunConfiguration_ = null;
    this.testCaseInfo_ = null;
    this.onTestDone_ = null;
    this.testIndex_ = 0;
    this.testCase_ = null;
    this.testName_ = null;
    this.start_ = null;
    this.errors_ = null;

    // Unset this callback before running it because the next callback may be
    // set by the code run by the callback.
    var onTestRunConfigurationComplete = this.onTestRunConfigurationComplete_;
    this.onTestRunConfigurationComplete_ = null;
    onTestRunConfigurationComplete.call(this);
  }
};


/**
 * Starts the next phase of the current test in the current test case. Creates a
 * DeferredQueue to manage the steps of this phase, executes the phase
 * catching any exceptions, and then hands the control over to the queue to
 * call onQueueComplete when it empties.
 */
jstestdriver.plugins.async.AsyncTestRunnerPlugin.prototype.execute_ = function(
    onStageComplete, invokeMethod) {
  var runner = this;
  var onError = function(error) {runner.errors_.push(error);};
  var stage = new jstestdriver.plugins.async.TestStage.Builder().
      setOnError(onError).
      setOnStageComplete(onStageComplete).
      setTestCase(this.testCase_).
      setTestMethod(invokeMethod).
      setPauseForHuman(this.pauseForHuman_).
      setArmorConstructor(this.armorConstructor_).
      setQueueConstructor(this.queueConstructor_).
      setTimeoutSetter(this.setTimeout_).
      setToJson(this.toJson_).
      build();
  stage.execute();
};


/**
 * Starts the setUp phase.
 */
jstestdriver.plugins.async.AsyncTestRunnerPlugin.prototype.startSetUp = function() {
  var runner = this;
  this.execute_(function(errors) {
    runner.finishSetUp(errors);
  }, this.testCase_['setUp']);
};

/**
 * Finishes the setUp phase and reports any errors. If there are errors it
 * initiates the tearDown phase, otherwise initiates the testMethod phase.
 *
 * @param errors errors caught during the current asynchronous phase.
 */
jstestdriver.plugins.async.AsyncTestRunnerPlugin.prototype.finishSetUp = function(errors) {
  this.errors_ = this.errors_.concat(errors);
  if (this.errors_.length) {
    this.startTearDown();
  } else {
    this.startTestMethod();
  }
};

/**
 * Starts the testMethod phase.
 */
jstestdriver.plugins.async.AsyncTestRunnerPlugin.prototype.startTestMethod = function() {
  var runner = this;
  this.execute_(function(errors) {
    runner.finishTestMethod(errors);
  }, this.testCase_[this.testName_]);
};

/**
 * Finishes the testMethod phase and reports any errors. Continues with the
 * tearDown phase.
 *
 * @param errors errors caught during the current asynchronous phase.
 */
jstestdriver.plugins.async.AsyncTestRunnerPlugin.prototype.finishTestMethod = function(errors) {
  this.errors_ = this.errors_.concat(errors);
  this.startTearDown();
};


/**
 * Start the tearDown phase.
 */
jstestdriver.plugins.async.AsyncTestRunnerPlugin.prototype.startTearDown = function() {
  var runner = this;
  this.execute_(function(errors){
    runner.finishTearDown(errors);
  }, this.testCase_['tearDown']);
};


/**
 * Finishes the tearDown phase and reports any errors. Submits the test results
 * to the test runner. Continues with the next test.
 *
 * @param errors errors caught during the current asynchronous phase.
 */
jstestdriver.plugins.async.AsyncTestRunnerPlugin.prototype.finishTearDown = function(errors) {
  this.errors_ = this.errors_.concat(errors);
  this.clearBody_();
  this.onTestDone_(this.buildResult());
  this.testIndex_ += 1;
  this.nextTest();
};

/**
 * Builds a test result.
 */
jstestdriver.plugins.async.AsyncTestRunnerPlugin.prototype.buildResult = function() {
  var end = new this.dateObj_().getTime();
  var result = jstestdriver.TestResult.RESULT.PASSED;
  var message = '';
  if (this.errors_.length) {
    result = jstestdriver.TestResult.RESULT.FAILED;
    message = this.toJson_(this.errors_);
  } else if (jstestdriver.expectedAssertCount != -1 &&
             jstestdriver.expectedAssertCount != jstestdriver.assertCount) {
    result = jstestdriver.TestResult.RESULT.FAILED;
    message = this.toJson_([new Error("Expected '" +
        jstestdriver.expectedAssertCount +
        "' asserts but '" +
        jstestdriver.assertCount +
        "' encountered.")]);
  }
  return new jstestdriver.TestResult(
      this.testCaseInfo_.getTestCaseName(), this.testName_, result, message,
      jstestdriver.console.getAndResetLog(), end - this.start_);
};
