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

goog.provide('jstestdriver.Console');


jstestdriver.JSON = window['JSON'] || {};
jstestdriver.JSON.stringify = window['JSON'] ? window['JSON']['stringify'] :
    function(msg, opt_args) {};


// TODO(corysmith): Separate this into a Utils namespace that can loaded earlier.
jstestdriver.FORMAT_MAPPINGS = {
  's' : function(arg) {
    if (arg == undefined) {
      return '';
    }
    return String(arg);
  },
  'd' : Number,
  'i' : parseInt,
  'f' : parseFloat,
  'o' : jstestdriver.JSON.stringify
};


jstestdriver.formatString = function(str) {
  var formatArgs = arguments;
  var idx = 1;
  var formatted = String(str).replace(/%([sdifo])/g,
      function(fullmatch, groupOne) {
    var value = formatArgs[idx++];
    if (!jstestdriver.FORMAT_MAPPINGS[groupOne]) {
      throw new Error(groupOne + 'is not a proper format.');
    }
    if (value === undefined || value === null) {
      return value;
    }
    return jstestdriver.FORMAT_MAPPINGS[groupOne](value);
  })
  while (idx < formatArgs.length) {
    var currentArg = formatArgs[idx++]
    if (typeof currentArg == 'object') {
      currentArg = jstestdriver.JSON.stringify(currentArg);
    }
    formatted += " " + currentArg;
  }
  return formatted;
};


/**
 * @constructor
 */
jstestdriver.Console = function() {
  this.log_ = [];
};


jstestdriver.Console.prototype.log = function() {
  this.logStatement('[LOG]', jstestdriver.formatString.apply(this, arguments));
};


jstestdriver.Console.prototype.debug = function() {
  this.logStatement('[DEBUG]', jstestdriver.formatString.apply(this, arguments));
};


jstestdriver.Console.prototype.info = function() {
  this.logStatement('[INFO]', jstestdriver.formatString.apply(this, arguments));
};


jstestdriver.Console.prototype.warn = function() {
  this.logStatement('[WARN]', jstestdriver.formatString.apply(this, arguments));
};


jstestdriver.Console.prototype.error = function() {
  this.logStatement('[ERROR]', jstestdriver.formatString.apply(this, arguments));
};


jstestdriver.Console.prototype.logStatement = function(level, statement) {
  this.log_.push(level + ' ' + statement);
};


jstestdriver.Console.prototype.getLog = function() {
  var log = this.log_;
  return log.join('\n');
};


jstestdriver.Console.prototype.getAndResetLog = function() {
  var log = this.getLog();
  this.log_ = [];
  return log;
};
