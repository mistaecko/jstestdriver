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
package com.google.jstestdriver.server.handlers.pages;

import java.util.Set;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FileSource;
import com.google.jstestdriver.FilesCache;
import com.google.jstestdriver.hooks.FileInfoScheme;
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.util.HtmlWriter;

/**
 * A utility that write The contents of the FilesCache into an HtmlWriter.
 *
 * @author Cory Smith (corbinrsmith@gmail.com) 
 */
// TODO(corysmith): Probably, horribly, misnamed. Fix.
public class TestFileUtil {
  private final HandlerPathPrefix prefix;
  private final FilesCache cache;
  private final Set<FileInfoScheme> schemes;
  private final Gson gson;

  /**
   * Creates a new TestFileUtil from the dependencies.
   */
  @Inject
   TestFileUtil(FilesCache cache, HandlerPathPrefix prefix, Set<FileInfoScheme> schemes,
      Gson gson) {
    this.cache = cache;
    this.prefix = prefix;
    this.schemes = schemes;
    this.gson = gson;
  }

  /**
   * Writes as many of the test files into the HtmlWriter, until it reaches a
   * FileInfo that cannot be handled using a &lt;link&gt; or a &lt;script&gt;.
   * @param writer The output writer.
   */
  public void writeTestFiles(HtmlWriter writer) {
    for (FileInfo file : cache.getAllFileInfos()) {
      if (file.isServeOnly()) {
        continue;
      }
      // TODO(corysmith): This is a problematic optimization.
      // If a client connects with unknown schemes and causes a reset, this will mess up miserably.
      // Must fix.
      FileSource fileSource = file.toFileSource(prefix, schemes);
      if (!(fileSource.getFileSrc().startsWith("http") || fileSource.getFileSrc().startsWith("/test"))) {
        // better safe than sorry.
        break;
      }
      writer.writeScript(String.format(
          "jstestdriver.manualResourceTracker.startResourceLoad('%s')",
          gson.toJson(fileSource)));
      if (fileSource.getFileSrc().endsWith(".css")) {
        writer.writeStyleSheet(fileSource.getFileSrc());
      } else {
        writer.writeExternalScript(fileSource.getFileSrc());
      }
      writer.writeScript("jstestdriver.manualResourceTracker.finishResourceLoad()");
    }
  }
}
