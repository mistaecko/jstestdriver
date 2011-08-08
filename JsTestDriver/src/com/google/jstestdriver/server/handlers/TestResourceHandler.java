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

import com.google.inject.Inject;
import com.google.jstestdriver.FilesCache;
import com.google.jstestdriver.requesthandlers.RequestHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
class TestResourceHandler implements RequestHandler {
  private static final Logger logger = LoggerFactory.getLogger(TestResourceHandler.class);

  private static final String TIME_IN_THE_PAST = "Sat, 22 Sep 1984 00:00:00 GMT";

  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final FilesCache filesCache;

  @Inject
  public TestResourceHandler(
      HttpServletRequest request,
      HttpServletResponse response,
      FilesCache filesCache) {
    this.request = request;
    this.response = response;
    this.filesCache = filesCache;
  }

  @Override
  public void handleIt() throws IOException {
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-Control", "private, no-cache, no-store, max-age=0, must-revalidate");
    response.setHeader("Expires", TIME_IN_THE_PAST);
    service(request.getPathInfo().substring(1) /* remove the first / */, response.getWriter());
  }

  public void service(String fileName, PrintWriter writer) throws IOException {
    try {
      String fileContent = filesCache.getFileContent(fileName);
      String mimeType = parseMimeType(fileName);
      if (mimeType != null) {
        response.setContentType(mimeType);
      } else {
        response.setHeader("Content-Type", "text/plain");
      }
      writer.write(fileContent);
      writer.flush();
    } catch (FilesCache.MissingFileException e) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  private String parseMimeType(String fileName) {
    int extension = fileName.lastIndexOf(".");
    if (extension == -1) {
      return null;
    }
    return StaticResourceHandler.MIME_TYPE_MAP.get(
        fileName.substring(extension + 1));
  }
}