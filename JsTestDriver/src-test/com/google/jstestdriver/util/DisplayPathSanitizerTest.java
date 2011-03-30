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
package com.google.jstestdriver.util;

import java.io.File;

import junit.framework.TestCase;

/**
 * Tests to ensure the display path is correctly sanitized.
 * Due to the use of the File.separator, this will act differently on different
 * operating systems. Intentionally.
 *
 * @author Cory Smith (corbinrsmith@gmail.com) 
 */
public class DisplayPathSanitizerTest extends TestCase {
  private DisplayPathSanitizer sanitizer;
  private File basePath;

  @Override
  protected void setUp() throws Exception {
    basePath = new File(".");
    sanitizer = new DisplayPathSanitizer(basePath);
  }
  
  public void testRemoveBasePath() throws Exception {
    String path = new File(basePath,
        "baz" + File.separator + "bar.js").getAbsolutePath();
    assertEquals("baz/bar.js", sanitizer.sanitize(path));
  }

  public void testLeaveAbsoluteWithoutBasePath() throws Exception {
    String path = File.separator + "baz" + File.separator + "bar.js";
    assertEquals("/baz/bar.js", sanitizer.sanitize(path));
  }
  
  public void testCleanupWindowsPath() throws Exception {
    assertEquals("/baz/bar.js", sanitizer.sanitize("\\baz\\bar.js"));
  }
}
