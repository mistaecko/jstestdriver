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
 * @fileoverview Defines the TestStage class.
 * @author rdionne@google.com (Robert Dionne)
 */

goog.provide('jstestdriver.plugins.async.TestStage');
goog.provide('jstestdriver.plugins.async.TestStage.Builder');

goog.require('jstestdriver');
goog.require('jstestdriver.setTimeout');
goog.require('jstestdriver.plugins.async.DeferredQueueArmor');
goog.require('jstestdriver.plugins.async.DeferredQueue');

/**
 * Constructs a TestStage.
 *
 * A TestStage is an executable portion of a test, such as setUp, tearDown, or
 * the test method.
 *
 * @param {Function} onError An error handler.
 * @param {Function} onStageComplete A callback for stage completion.
 * @param {Object} testCase The test case that owns this test stage.
 * @param {Function} testMethod The test method this stage represents.
 * @param {function(Object)} toJson a function to convert objects to JSON.
 * @param {Object} opt_argument An argument to pass to the test method.
 * @param {boolean} opt_pauseForHuman Whether to pause for debugging.
 * @param {Function} opt_armorConstructor The constructor of DeferredQueueArmor.
 * @param {Function} opt_queueConstructor The constructor of DeferredQueue.
 * @param {Function} opt_setTimeout The setTimeout function or suitable
 *     replacement.
 * @constructor
 */
jstestdriver.plugins.async.TestStage = function(
    onError, onStageComplete, testCase, testMethod, toJson, opt_argument,
    opt_pauseForHuman, opt_armorConstructor, opt_queueConstructor,
    opt_setTimeout) {
  this.onError_ = onError;
  this.onStageComplete_ = onStageComplete;
  this.testCase_ = testCase;
  this.testMethod_ = testMethod;
  this.toJson_ = toJson;
  this.argument_ = opt_argument;
  this.pauseForHuman_ = !!opt_pauseForHuman;
  this.armorConstructor_ = opt_armorConstructor ||
      jstestdriver.plugins.async.DeferredQueueArmor;
  this.queueConstructor_ = opt_queueConstructor ||
      jstestdriver.plugins.async.DeferredQueue;
  this.setTimeout_ = opt_setTimeout || jstestdriver.setTimeout;
};


/**
 * Executes this TestStage.
 */
jstestdriver.plugins.async.TestStage.prototype.execute = function() {
  var armor = new (this.armorConstructor_)(this.toJson_);
  var queue = new (this.queueConstructor_)(this.setTimeout_, this.testCase_,
      this.onStageComplete_, armor, this.pauseForHuman_);
  armor.setQueue(queue);

  if (this.testMethod_) {
    try {
      this.testMethod_.call(this.testCase_, armor, this.argument_);
    } catch (e) {
      this.onError_(e);
    }
  }

  queue.startStep();
};



/**
 * Constructor for a Builder of TestStages. Used to avoid confusion when
 * trying to construct TestStage objects (as the constructor takes a lot
 * of parameters of similar types).
 * @constructor
 */
jstestdriver.plugins.async.TestStage.Builder = function() {
  this.onError_ = null;
  this.onStageComplete_ = null;
  this.testCase_ = null;
  this.testMethod_ = null;
  this.toJson_ = null;
  this.opt_argument_ = null;
  this.opt_pauseForHuman_ = null;
  this.opt_armorConstructor_ = jstestdriver.plugins.async.DeferredQueueArmor;
  this.opt_queueConstructor_ = jstestdriver.plugins.async.DeferredQueue;
  this.opt_setTimeout_ = jstestdriver.setTimeout;
};


// Setters for the various fields; they return the Builder instance to allow
// method call chaining.
jstestdriver.plugins.async.TestStage.Builder.prototype.setOnError =
    function(onError) {
  this.onError_ = onError;
  return this;
};


jstestdriver.plugins.async.TestStage.Builder.prototype.setOnStageComplete =
    function(onStageComplete) {
  this.onStageComplete_ = onStageComplete;
  return this;
};


jstestdriver.plugins.async.TestStage.Builder.prototype.setTestCase =
    function(testCase) {
  this.testCase_ = testCase;
  return this;
};


jstestdriver.plugins.async.TestStage.Builder.prototype.setTestMethod =
    function(testMethod) {
  this.testMethod_ = testMethod;
  return this;
};


jstestdriver.plugins.async.TestStage.Builder.prototype.setToJson =
    function(toJson) {
  this.toJson_ = toJson;
  return this;
};


jstestdriver.plugins.async.TestStage.Builder.prototype.setArgument =
    function(argument) {
  this.opt_argument_ = argument;
  return this;
};


jstestdriver.plugins.async.TestStage.Builder.prototype.setPauseForHuman =
    function(pauseForHuman) {
  this.opt_pauseForHuman_ = pauseForHuman;
  return this;
};


jstestdriver.plugins.async.TestStage.Builder.prototype.setArmorConstructor =
    function(armorConstructor) {
  this.opt_armorConstructor_ = armorConstructor;
  return this;
};


jstestdriver.plugins.async.TestStage.Builder.prototype.setQueueConstructor =
    function(queueConstructor) {
  this.opt_queueConstructor_ = queueConstructor;
  return this;
};


jstestdriver.plugins.async.TestStage.Builder.prototype.setTimeoutSetter =
    function(setTimeout) {
  this.opt_setTimeout_ = setTimeout;
  return this;
};


jstestdriver.plugins.async.TestStage.Builder.prototype.build = function() {
  return new jstestdriver.plugins.async.TestStage(
      this.onError_, this.onStageComplete_, this.testCase_, this.testMethod_,
      this.toJson_, this.opt_argument_, this.opt_pauseForHuman_,
      this.opt_armorConstructor_, this.opt_queueConstructor_,
      this.opt_setTimeout_);
};
