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
 * @fileoverview Defines the FiniteUseCallback class, which decorates a
 * Javascript function by notifying the test runner about any exceptions thrown
 * when the function executes.
 *
 * @author rdionne@google.com (Robert Dionne)
 */

goog.provide('jstestdriver.plugins.async.CatchingCallback');

goog.require('jstestdriver');

/**
 * Constructs a CatchingCallback.
 *
 * @param {Object} testCase the testCase to use as 'this' when calling the
 *    wrapped function.
 * @param {jstestdriver.plugins.async.CallbackPool} pool the pool to which this
 *    callback belongs.
 * @param {Function} wrapped the wrapped callback function.
 * @constructor
 */
jstestdriver.plugins.async.CatchingCallback = function(
    testCase, pool, wrapped) {
  this.testCase_ = testCase;
  this.pool_ = pool;
  this.callback_ = wrapped;
};


/**
 * Invokes the wrapped callback, catching any exceptions and reporting the
 * status to the pool.
 * @return {*} The return value of the original callback.
 */
jstestdriver.plugins.async.CatchingCallback.prototype.invoke = function() {
  var result;
  var message;
  try {
    result = this.callback_.apply(this.testCase_, arguments);
    message = 'success.';
    return result;
  } catch (e) {
    this.pool_.onError(e);
    message = 'failure: ' + e;
    throw e;
  } finally {
    this.pool_.remove(message);
  }
};
