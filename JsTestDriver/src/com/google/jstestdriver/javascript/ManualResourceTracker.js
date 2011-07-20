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


jstestdriver.ManualResourceTracker = function(
    parse,
    serialize,
    pluginRegistrar,
    getBrowserInfo,
    manualScriptLoader) {
  this.parse_ = parse;
  this.serialize_ = serialize;
  this.getBrowserInfo_ = getBrowserInfo;
  this.manualScriptLoader_ = manualScriptLoader;
  this.boundOnComplete_ = jstestdriver.bind(this, this.onComplete_);
  this.results_ = [];
};

/**
 * Starts the resource load tracking to catch errors and other statistics. 
 * @param {String} jsonFile A serialized jstestdriver.FileSrc
 */
jstestdriver.ManualResourceTracker.prototype.startResourceLoad =
    function(jsonFile) {
  var file = this.parse_(jsonFile);
  this.manualScriptLoader_.beginLoad(file, jstestdriver.bind(this, this.onComplete_));
};

/**
 * Method to be called with the resource completes.
 * @param {jstestdriver.FileLoadResult} result
 */
jstestdriver.ManualResourceTracker.prototype.onComplete_ = function(result) {
  this.results_.push(result);
};

/**
 * Called after the resource has loaded (maybe, other times it will called immediately).
 */
jstestdriver.ManualResourceTracker.prototype.finishResourceLoad = function() {
  this.manualScriptLoader_.endLoad();
};

/**
 * Returns the collected results from loading.
 * @return {Array.<jstestdriver.FileLoadResult>}
 */
jstestdriver.ManualResourceTracker.prototype.getResults = function() {
  return this.results_;
};
