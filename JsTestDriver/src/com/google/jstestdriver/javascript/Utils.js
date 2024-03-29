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


jstestdriver.convertToJson = function(delegate) {
  var serialize = jstestdriver.parameterSerialize
  return function(url, data, callback, type) {
    delegate(url, serialize(data), callback, type);
  };
};


jstestdriver.parameterSerialize = function(data) {
  var modifiedData = {};
  for (var key in data) {
    modifiedData[key] = JSON.stringify(data[key]);
  }
  return modifiedData;
};


jstestdriver.bind = function(context, func) {
  function bound() {
    return func.apply(context, arguments);
  };
  bound.toString = function() {
    return "bound: " + context + " to: " + func;
  }
  return bound;
};


jstestdriver.extractId = function(url) {
  return url.match(/\/id\/(\d+)\//)[1];
};


jstestdriver.createPath = function(basePath, path) {
  var prefix = basePath.match(/^(.*)\/(slave|runner|bcr)\//)[1];
  return prefix + path;
};


jstestdriver.getBrowserFriendlyName = function() {
  if (jstestdriver.jQuery.browser.safari) {
    if (navigator.userAgent.indexOf('Chrome') != -1) {
      return 'Chrome';
    }
    return 'Safari';
  } else if (jstestdriver.jQuery.browser.opera) {
    return 'Opera';
  } else if (jstestdriver.jQuery.browser.msie) {
    return 'Internet Explorer';
  } else if (jstestdriver.jQuery.browser.mozilla) {
    if (navigator.userAgent.indexOf('Firefox') != -1) {
      return 'Firefox';
    }
    return 'Mozilla';
  }
};


jstestdriver.getBrowserFriendlyVersion = function() {
  if (jstestdriver.jQuery.browser.msie) {
    if (typeof XDomainRequest != 'undefined') {
      return '8.0';
    } 
  } else if (jstestdriver.jQuery.browser.safari) {
    if (navigator.appVersion.indexOf('Chrome/') != -1) {
      return navigator.appVersion.match(/Chrome\/(.*)\s/)[1];
    }
  }
  return jstestdriver.jQuery.browser.version;
};

jstestdriver.trim = function(str) {
  return str.replace(/(^\s*)|(\s*$)/g,'');
};


/**
 * Renders an html string as a dom nodes.
 * @param {string} htmlString The string to be rendered as html.
 * @param {Document} owningDocument The window that should own the html.
 */
jstestdriver.toHtml = function(htmlString, owningDocument) {
  var fragment = owningDocument.createDocumentFragment();
  var wrapper = owningDocument.createElement('div');
  wrapper.innerHTML = jstestdriver.trim(jstestdriver.stripHtmlComments(htmlString));
  while(wrapper.firstChild) {
    fragment.appendChild(wrapper.firstChild);
  }
  var ret =  fragment.childNodes.length > 1 ? fragment : fragment.firstChild;
  return ret;
};


jstestdriver.stripHtmlComments = function(htmlString) {
  var stripped = [];
  function getCommentIndices(offset) {
    var start = htmlString.indexOf('<!--', offset);
    var stop = htmlString.indexOf('-->', offset) + '-->'.length;
    if (start == -1) {
      return null;
    }
    return {
      'start' : start,
      'stop' : stop
    };
  }
  var offset = 0;
  while(true) {
    var comment = getCommentIndices(offset);
    if (!comment) {
      stripped.push(htmlString.slice(offset));
      break;
    }
    var frag = htmlString.slice(offset, comment.start);
    stripped.push(frag);
    offset = comment.stop;
  }
  return stripped.join('');
}


/**
 * Appends html string to the body.
 * @param {string} htmlString The string to be rendered as html.
 * @param {Document} owningDocument The window that should own the html.
 */
jstestdriver.appendHtml = function(htmlString, owningDocument) {
  var node = jstestdriver.toHtml(htmlString, owningDocument);
  jstestdriver.jQuery(owningDocument.body).append(node);
};


/**
 * @return {Number} The ms since the epoch.
 */
jstestdriver.now = function() { return new Date().getTime();}


/**
 * Creates a wrapper for jQuery.ajax that make a synchronous post
 * @param {jQuery} jQuery
 * @return {function(url, data):null}
 */
jstestdriver.createSynchPost = function(jQuery) {
  return jstestdriver.convertToJson(function(url, data) {
    return jQuery.ajax({
      'async' : false,
      'data' : data,
      'type' : 'POST',
      'url' : url
    });
  });
};

jstestdriver.utils = {};

jstestdriver.utils.serializeObject = function(obj) {
  var out = [];
  jstestdriver.utils.serializeObjectToArray(obj, out);
  return out.join('');
};


jstestdriver.utils.serializeObjectToArray =
   function(obj, opt_out){
  var out = opt_out || out;
  if (obj instanceof Array) {
      out.push('[');
      var arr = /** @type {Array.<Object>} */
      obj;
      for ( var i = 0; i < arr.length; i++) {
        this.serializeObjectToArray(arr[i], out);
        if (i < arr.length - 1) {
          out.push(',');
        }
      }
      out.push(']');
    } else if (obj instanceof Error) {
      out.push('{');
      out.push('"message":');
      this.serializeObjectToArray(obj.message, out);
      this.serializePropertyOnObject('name', obj, out);
      this.serializePropertyOnObject('description', obj, out);
      this.serializePropertyOnObject('fileName', obj, out);
      this.serializePropertyOnObject('lineNumber', obj, out);
      this.serializePropertyOnObject('number', obj, out);
      this.serializePropertyOnObject('stack', obj, out);
      out.push('}');
    } else {
      out.push(jstestdriver.angular.toJson(obj));
    }
    return out;
  };


jstestdriver.utils.serializePropertyOnObject = function(name, obj, out) {
  if (name in obj) {
    out.push(',');
    out.push('"' + name + '":');
    this.serializeObjectToArray(obj[name], out);
  }
};


