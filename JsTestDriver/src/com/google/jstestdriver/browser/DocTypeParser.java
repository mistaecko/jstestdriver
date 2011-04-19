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
package com.google.jstestdriver.browser;

import com.google.jstestdriver.config.ConfigurationException;


/**
 * 
 * 
 * @author Cory Smith (corbinrsmith@gmail.com)
 */
public class DocTypeParser {
  public DocType parse(String unparsed) {
    if ("quirks".equals(unparsed)) {
      return DocType.QUIRKS;
    }
    if ("strict".equals(unparsed)) {
      return DocType.STRICT;
    }
    if (unparsed.startsWith("<!DOCTYPE")) {
      return new DocType(unparsed);
    }
    throw new ConfigurationException("Invalid custom doctype: " + unparsed);
  }
}
