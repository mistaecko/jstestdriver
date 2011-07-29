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
 * @fileoverview Defines the ExpiringCallback class, which decorates a
 * Javascript function by restricting the length of time the asynchronous system
 * may delay before calling the function.
 *
 * @author rdionne@google.com (Robert Dionne)
 */

goog.provide('jstestdriver.plugins.async.ExpiringCallback');

goog.require('jstestdriver');

/**
 * Constructs an ExpiringCallback.
 *
 * @param {jstestdriver.plugins.async.CallbackPool} pool The pool to which this
 *     callback belongs.
 * @param {jstestdriver.plugins.async.FiniteUseCallback} callback A
 *     FiniteUseCallback.
 * @param {jstestdriver.plugins.async.Timeout} timeout A Timeout object.
 * @param {string} stepDescription A description of the current test step.
 * @constructor
 */
jstestdriver.plugins.async.ExpiringCallback = function(
    pool, callback, timeout, stepDescription, callbackDescription) {
  this.pool_ = pool;
  this.callback_ = callback;
  this.timeout_ = timeout;
  this.stepDescription_ = stepDescription;
  this.callbackDescription_ = callbackDescription;
};


/**
 * Arms this callback to expire after the given delay.
 *
 * @param {number} delay The amount of time (ms) before this callback expires.
 */
jstestdriver.plugins.async.ExpiringCallback.prototype.arm = function(delay) {
  var callback = this;
  this.timeout_.arm(function() {
    callback.pool_.onError(new Error('Callback \'' +
        callback.callbackDescription_ + '\' expired after ' + delay +
        ' ms during test step \'' + callback.stepDescription_ + '\''));
    callback.pool_.remove('expired.', callback.callback_.getRemainingUses());
    callback.callback_.deplete();
  }, delay);
};


/**
 * Invokes this callback.
 * @return {*} The return value of the FiniteUseCallback.
 */
jstestdriver.plugins.async.ExpiringCallback.prototype.invoke = function() {
  return this.callback_.invoke.apply(this.callback_, arguments);
};

