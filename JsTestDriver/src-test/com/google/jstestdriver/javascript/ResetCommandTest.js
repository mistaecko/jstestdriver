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
var ResetCommandTest = jstestdriver.testCaseManager.TestCase('ResetCommandTest');


ResetCommandTest.prototype.testResetDefault = function() {
  var signal = new jstestdriver.Signal(true);
  var now = 1;
  
  var locationMock = {
    search : "",
    protocol : "http",
    host : "host",
    pathname : "/path/name",
    reload : function(newUrl) {
      this.newUrl = newUrl;
    }
  };

  var command = new jstestdriver.ResetCommand(signal,
      locationMock,
      function() {
    return now;
  });
  
  command.reset([]);
  
  assertEquals("http://host/path/name/refresh/" + now + "")
};


ResetCommandTest.prototype.testResetDefault = function() {
  var signal = new jstestdriver.Signal(true);
  var now = 1;
  
  var locationMock = {
      search : "",
      protocol : "http:",
      host : "host",
      pathname : "/path/name",
      replace : function(newUrl) {
        this.newUrl = newUrl;
      }
  };
  
  var command = new jstestdriver.ResetCommand(
      locationMock,
      signal,
      function() {
    return now;
  });
  
  command.reset([]);
  
  assertEquals("http://host/path/name/refresh/" + now + "/load_type/load", locationMock.newUrl)
};


ResetCommandTest.prototype.testResetLoadType = function() {
  var signal = new jstestdriver.Signal(true);
  var now = 1;
  
  var locationMock = {
      search : "",
      protocol : "http:",
      host : "host",
      pathname : "/path/name",
      replace : function(newUrl) {
        this.newUrl = newUrl;
      }
  };
  
  var command = new jstestdriver.ResetCommand(
      locationMock,
      signal,
      function() {
    return now;
  });
  
  command.reset(["load"]);
  assertEquals("http://host/path/name/refresh/" + now + "/load_type/load", locationMock.newUrl)
};
