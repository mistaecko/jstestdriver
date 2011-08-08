/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License'); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

var ServeTest = AsyncTestCase('ServeTest');

ServeTest.prototype.test404 = function(q) {
  q.defer('Send XHR', function(p) {
    jstestdriver.jQuery.ajax({
      dataType : 'text',
      url : '/test/src-test/com/google/jstestdriver/javascript/notthere.txt',
      complete : p.add('onComplete',function(xhr, statusText) {
        assertEquals(404, xhr.status);
      })
    });
  });
};

ServeTest.prototype.testSuccess = function(q) {
  q.defer(function(p) {
    jstestdriver.jQuery.ajax({
      dataType : 'text',
      url : '/test/src-test/com/google/jstestdriver/javascript/served.txt',
      complete : p.add('onComplete',function(xhr, statusText) {
        assertEquals(200, xhr.status);
        assertEquals('served', xhr.responseText);
      })
    });
  });
};
