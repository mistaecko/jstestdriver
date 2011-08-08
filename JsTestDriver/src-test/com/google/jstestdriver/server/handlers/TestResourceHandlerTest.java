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
package com.google.jstestdriver.server.handlers;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.easymock.EasyMock;

import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FilesCache;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class TestResourceHandlerTest extends TestCase {

  private ByteArrayOutputStream out = new ByteArrayOutputStream();
  private PrintWriter writer = new PrintWriter(out);

  public void testEmptyReturnWhenFileNotPresent() throws Exception {
    HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
    response.sendError(404);
    EasyMock.expectLastCall().anyTimes();
    EasyMock.replay(response);
    TestResourceHandler handler =
        new TestResourceHandler(null, response, new FilesCache(new HashMap<String, FileInfo>()));

    handler.service("nothing", writer);
  }

  public void testServeFile() throws Exception {
    HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
    response.setContentType(StaticResourceHandler.MIME_TYPE_MAP.get("js"));
    EasyMock.expectLastCall().anyTimes();
    EasyMock.replay(response);
    Map<String, FileInfo> files = new HashMap<String, FileInfo>();

    files.put("dummy.js", new FileInfo("dummy.js", -1, -1, false, false, "data", "dummy.js"));
    files.put("dummytoo.js", new FileInfo("dummytoo.js", 20, -1, false, false, "more data",
        "dummytoo.js"));
    FilesCache filesCache = new FilesCache(files);
    TestResourceHandler handler = new TestResourceHandler(null, response, filesCache);

    handler.service("dummy.js", writer);
    assertEquals("data", out.toString());
    out.reset();
    handler.service("dummytoo.js", writer);
    assertEquals("more data", out.toString());
  }
}
