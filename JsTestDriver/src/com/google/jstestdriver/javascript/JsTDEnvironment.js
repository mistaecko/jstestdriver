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
 * @fileDescription
 * Provides the base jstestdriver environment constants and functions.
 */

goog.provide('jstestdriver');
goog.provide('jstestdriver.setTimeout');

goog.require('jstestdriver.Console');


jstestdriver.global = this;
jstestdriver.console = null;
jstestdriver.console = new jstestdriver.Console();
jstestdriver.JSON = JSON;

jstestdriver.SERVER_URL = "/query/";

jstestdriver.HEARTBEAT_URL = "/heartbeat";

window['console'] = window['console'] || {};
if (typeof window['console']['log'] == 'undefined') window['console']['log'] = function(msg) {};
if (typeof window['console']['debug'] == 'undefined') window['console']['debug'] = function(msg) {};
if (typeof ['console']['info'] == 'undefined') window['console']['info'] = function(msg) {};
if (typeof ['console']['warn'] == 'undefined') window['console']['warn'] = function(msg) {};
if (typeof ['console']['error'] == 'undefined') window['console']['error'] = function(msg) {};

jstestdriver.globalSetTimeout = window.setTimeout;
jstestdriver.setTimeout = function() {
  if (jstestdriver.globalSetTimeout.apply) {
    return jstestdriver.globalSetTimeout.apply(window, arguments);
  }
  return jstestdriver.globalSetTimeout(arguments[0], arguments[1]);
};

jstestdriver.globalClearTimeout = clearTimeout;
jstestdriver.clearTimeout = function() {
  if (jstestdriver.globalClearTimeout.apply) {
    return jstestdriver.globalClearTimeout.apply(window, arguments);
  }
  return jstestdriver.globalClearTimeout(arguments[0]);
};

jstestdriver.globalSetInterval = setInterval;
jstestdriver.setInterval = function() {
  if (jstestdriver.globalSetInterval.apply) {
    return jstestdriver.globalSetInterval.apply(window, arguments);
  }
  return jstestdriver.globalSetInterval(arguments[0], arguments[1]);
};

//TODO(corysmith): make this configurable.
jstestdriver.log = function(message) {
  /*if (window.console && window.console.log) {
    window.console.log(message);
  }*/
};

jstestdriver.globalClearInterval = clearInterval;
jstestdriver.clearInterval = function() {
  if (jstestdriver.globalClearInterval.apply) {
    return jstestdriver.globalClearInterval.apply(window, arguments);
  }
  return jstestdriver.globalClearInterval(arguments[0]);
};

document.write = function(str) {
  //jstestdriver.console.error('Illegal call to document.write.');
};


var noop = jstestdriver.EMPTY_FUNC = function() {};
