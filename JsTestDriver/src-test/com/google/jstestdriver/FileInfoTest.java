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
package com.google.jstestdriver;

import junit.framework.TestCase;

/**
 * @author andrewtrenk
 */
public class FileInfoTest extends TestCase {

  public void testIsWebAddress() {
    FileInfo httpFile =
        new FileInfo("http://www.google.com", 0, -1, false, false, null, "http://www.google.com");
    FileInfo httpsFile =
        new FileInfo("https://www.google.com", 0, -1, false, false, null, "http://www.google.com");
    FileInfo nonWebFile = new FileInfo("a/b/c/file.js", 0, -1, false, false, null, "a/b/c/file.js");

    assertTrue(httpFile.isWebAddress());
    assertTrue(httpsFile.isWebAddress());
    assertFalse(nonWebFile.isWebAddress());
  }
  
  public void testShouldReplaceWith() throws Exception {
    FileInfo oldFile = new FileInfo("a/b/c/file.js", 0, -1, false, false, null, "a/b/c/file.js");
    FileInfo newFile = new FileInfo("a/b/c/file.js", 10, -1, false, false, null, "a/b/c/file.js");
    
    assertTrue("A different timestamp means replace", oldFile.shouldReplaceWith(newFile));
    assertFalse("Same timestamp and length means noreplace", oldFile.shouldReplaceWith(oldFile));
    /*TODO: uncomment this test when the FileInfos stop being sent form the browser with a -1 length.
    FileInfo longer = new FileInfo("a/b/c/file.js", 0, 10, false, false, null, "a/b/c/file.js");
    FileInfo shorter = new FileInfo("a/b/c/file.js", 0, 3, false, false, null, "a/b/c/file.js");
    
    assertTrue("A different length means replace", longer.shouldReplaceWith(shorter));
    */
  }
}
