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
 * @fileoverview Defines the DeferredQueue class.
 *
 * @author rdionne@google.com (Robert Dionne)
 */

goog.provide('jstestdriver.plugins.async.DeferredQueue');

goog.require('jstestdriver');
goog.require('jstestdriver.plugins.async.DeferredQueueArmor');
goog.require('jstestdriver.plugins.async.CallbackPool');
goog.require('jstestdriver.plugins.async.CallbackPoolArmor');

/**
 * Constructs a DeferredQueue.
 * @param {Function} setTimeout The setTimeout function.
 * @param {Object} testCase The test case that owns this queue.
 * @param {Function} onQueueComplete The queue complete callback.
 * @param {jstestdriver.plugins.async.DeferredQueueArmor} armor The armor
 *     wrapping all DeferredQueues for this test run0.
 * @param {boolean} opt_pauseForHuman Whether or not to pause for debugging.
 * @param {Function} opt_queueConstructor The DeferredQueue constructor.
 * @param {Function} opt_queueArmorConstructor The DeferredQueueArmor
 *     constructor.
 * @param {Function} opt_poolConstructor The CallbackPool constructor.
 * @param {Function} opt_poolArmorConstructor The CallbackPoolArmor constructor.
 * @constructor
 */
jstestdriver.plugins.async.DeferredQueue = function(setTimeout, testCase,
    onQueueComplete, armor, opt_pauseForHuman, opt_queueConstructor,
    opt_queueArmorConstructor, opt_poolConstructor, opt_poolArmorConstructor) {
  this.setTimeout_ = setTimeout;
  this.testCase_ = testCase;
  this.onQueueComplete_ = onQueueComplete;
  this.armor_ = armor;
  this.pauseForHuman_ = !!opt_pauseForHuman;
  this.queueConstructor_ = opt_queueConstructor ||
      jstestdriver.plugins.async.DeferredQueue;
  this.queueArmorConstructor_ = opt_queueArmorConstructor ||
      jstestdriver.plugins.async.DeferredQueueArmor;
  this.poolConstructor_ = opt_poolConstructor ||
      jstestdriver.plugins.async.CallbackPool;
  this.poolArmorConstructor_ = opt_poolArmorConstructor ||
      jstestdriver.plugins.async.CallbackPoolArmor;
  this.descriptions_ = [];
  this.operations_ = [];
  this.errors_ = [];
};


/**
 * Executes a step of the test.
 * @param {Function} operation The next test step.
 * @param {Function} onQueueComplete The queue complete callback.
 * @private
 */
jstestdriver.plugins.async.DeferredQueue.prototype.execute_ = function(
    description, operation, onQueueComplete) {
  var queue = new (this.queueConstructor_)(this.setTimeout_,
      this.testCase_, onQueueComplete, this.armor_, this.pauseForHuman_);
  this.armor_.setQueue(queue);

  var onPoolComplete = function(errors) {
    queue.finishStep_(errors);
  };
  var pool = new (this.poolConstructor_)(
      this.setTimeout_, this.testCase_, onPoolComplete, description, this.pauseForHuman_);
  var poolArmor = new (this.poolArmorConstructor_)(pool);

  if (operation) {
    try {
      operation.call(this.testCase_, poolArmor, this.armor_);
    } catch (e) {
      pool.onError(e);
    }
  }

  pool.activate();
};


/**
 * Enqueues a test step.
 * @param {string} description The test step description.
 * @param {Function} operation The test step to add to the queue.
 */
jstestdriver.plugins.async.DeferredQueue.prototype.defer = function(
    description, operation) {
  this.descriptions_.push(description);
  this.operations_.push(operation);
};


/**
 * Starts the next test step.
 */
jstestdriver.plugins.async.DeferredQueue.prototype.startStep = function() {
  var nextDescription = this.descriptions_.shift();
  var nextOp = this.operations_.shift();
  if (nextOp) {
    var q = this;
    this.execute_(nextDescription, nextOp, function(errors) {
      q.finishStep_(errors);
    });
  } else {
    this.onQueueComplete_([]);
  }
};


/**
 * Finishes the current test step.
 * @param {Array.<Error>} errors An array of any errors that occurred during the
 *     previous test step.
 * @private
 */
jstestdriver.plugins.async.DeferredQueue.prototype.finishStep_ = function(
    errors) {
  this.errors_ = this.errors_.concat(errors);
  if (this.errors_.length) {
    this.onQueueComplete_(this.errors_);
  } else {
    this.startStep();
  }
};
