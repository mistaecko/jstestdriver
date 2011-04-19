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

/**
 * The test page document type.
 *
 * @author Cory Smith (corbinrsmith@gmail.com) 
 */
public class DocType {

  public static final DocType QUIRKS = new DocType("<!DOCTYPE html PUBLIC>");

  public static final DocType STRICT =
      new DocType("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"" +
      " \"http://www.w3.org/TR/html4/strict.dtd\">");

  private final String markup;

  /**
   * @param markup The string representation of the doctype.
   */
  public DocType(String markup) {
    this.markup = markup;
  }

  /**
   * @return Returns the html complaint representation of the doctype.
   */
  public String toHtml() {
    return markup;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((markup == null) ? 0 : markup.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    DocType other = (DocType) obj;
    if (markup == null) {
      if (other.markup != null) return false;
    } else if (!markup.equals(other.markup)) return false;
    return true;
  }
}
