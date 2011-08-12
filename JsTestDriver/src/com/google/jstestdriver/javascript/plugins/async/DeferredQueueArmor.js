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
 * @fileoverview Defines the DeferredQueueArmor class. Encapsulates a
 * DeferredQueue behind a narrower interface. Also, validates arguments.
 *
 * @author rdionne@google.com (Robert Dionne)
 */

goog.provide('jstestdriver.plugins.async.DeferredQueueArmor');

goog.require('jstestdriver');

/**
 * Constructs a DeferredQueueArmor.
 * @param {function(Object)} toJson a function to convert objects to JSON.
 * @constructor
 */
jstestdriver.plugins.async.DeferredQueueArmor = function(toJson) {
  this.toJson_ = toJson;
  this.q_ = null;
  this.step_ = 1;
};


/**
 * Sets the current queue instance.
 * @param {jstestdriver.plugins.async.DeferredQueue} queue The queue.
 */
jstestdriver.plugins.async.DeferredQueueArmor.prototype.setQueue = function(
    queue) {
  this.q_ = queue;
};


/**
 * Adds a function to the queue to call later.
 * @param {string|Function} description The description or function.
 * @param {Function=} operation The function.
 * @return {jstestdriver.plugins.async.DeferredQueueArmor} This.
 */
jstestdriver.plugins.async.DeferredQueueArmor.prototype.call = function(
    description, operation) {
  if (!this.q_) {
    throw new Error('Queue undefined!');
  }

  if (typeof description == 'function') {
    operation = description;
    description = this.nextDescription_();
  }

  if (typeof description == 'object') {
    operation = description.operation;
    description = description.description;
  }

  if (!description) {
    description = this.nextDescription_();
  }

  if (operation) {
    this.q_.defer(description, operation);
    this.step_ += 1;
  }

  return this;
};


/**
 * @return {string} A description for the next step.
 */
jstestdriver.plugins.async.DeferredQueueArmor.prototype.nextDescription_ =
    function() {
  return '#' + this.step_;
};


/**
 * Adds a function to the queue to call later.
 * @param {string|Function} description The description or function.
 * @param {Function=} operation The function.
 * @deprecated Use DeferredQueueArmor#call().
 */
jstestdriver.plugins.async.DeferredQueueArmor.prototype.defer =
    jstestdriver.plugins.async.DeferredQueueArmor.prototype.call;
