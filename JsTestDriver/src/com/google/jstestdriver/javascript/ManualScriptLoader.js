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



jstestdriver.ManualScriptLoader = function(win, testCaseManager, now) {
  this.win_ = win;
  this.testCaseManager_ = testCaseManager;
  this.now_ = now;
  this.onFileLoaded_ = null;
  this.started_ = -1;
  this.file_ = null;
};


jstestdriver.ManualScriptLoader.prototype.beginLoad = function(file, onFileLoaded) {
  this.testCaseManager_.removeTestCaseForFilename(file.fileSrc);
  var handleError = this.createErrorHandler(file);
  this.win_.onerror = handleError;
  this.file_ = file;
  this.started_ = this.now_();
  this.onFileLoaded_ = onFileLoaded;
};


jstestdriver.ManualScriptLoader.prototype.endLoad = function() {
  if (this.file_) {
    var file = this.file_;
    this.file_ = null;
    this.testCaseManager_.updateLatestTestCase(file.fileSrc);
    var result = new jstestdriver.FileResult(file, true, '', this.now_() - this.started_);
    this.win_.onerror = jstestdriver.EMPTY_FUNC;
    this.onFileLoaded_(result);
  }
};


jstestdriver.ManualScriptLoader.prototype.createErrorHandler = function(file) {
  return jstestdriver.bind(this, function(msg, url, line) {
    var started = this.started_;
    this.started_ = -1;
    var file = this.file_;
    this.file_ = null;
    var loadMsg = 'error loading file: ' + file.fileSrc;

    if (line != undefined && line != null) {
      loadMsg += ':' + line;
    }

    if (msg != undefined && msg != null) {
      loadMsg += ': ' + msg;
    }
    this.win_.onerror = jstestdriver.EMPTY_FUNC;
    this.onFileLoaded_(new jstestdriver.FileResult(file, false, loadMsg, this.now_() - started));
  });
};
