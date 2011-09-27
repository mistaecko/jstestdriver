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
package com.google.jstestdriver.config;

import com.google.gson.JsonArray;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.Flags;
import com.google.jstestdriver.PathResolver;
import com.google.jstestdriver.Plugin;
import com.google.jstestdriver.browser.DocType;
import com.google.jstestdriver.model.HandlerPathPrefix;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * A configuration were all the paths have been resolved.
 * @author corbinrsmith@gmail.com (Cory Smith)
 *
 */
public class ResolvedConfiguration implements Configuration {

  private final Set<FileInfo> filesList;
  private final List<Plugin> plugins;
  private final String server;
  private final long testSuiteTimeout;
  private final List<FileInfo> tests;
  private final File basePath;
  private final JsonArray gatewayConfig;

  public ResolvedConfiguration(Set<FileInfo> filesList,
                               List<Plugin> plugins,
                               String server,
                               long testSuiteTimeout,
                               File basePath,
                               List<FileInfo> tests,
                               JsonArray gatewayConfig) {
    this.filesList = filesList;
    this.plugins = plugins;
    this.server = server;
    this.testSuiteTimeout = testSuiteTimeout;
    this.basePath = basePath;
    this.tests = tests;
    this.gatewayConfig = gatewayConfig;
  }

  // TODO(corysmith): fix this interface to not require extra information.
  public String getServer(String flagValue, int port, HandlerPathPrefix handlerPrefix) {
    if (flagValue != null && flagValue.length() != 0) {
      return handlerPrefix.suffixServer(flagValue);
    }
    if (server.length() > 0) {
      return handlerPrefix.suffixServer(server);
    }
    if (port == -1) {
      throw new ConfigurationException("Oh Snap! No server defined!");
    }

    return handlerPrefix.suffixServer(String.format("http://%s:%d", "127.0.0.1", port));
  }

  @Override
  public String getCaptureAddress(String server, String captureAddress,
      HandlerPathPrefix prefix) {
    if (captureAddress != null && !captureAddress.isEmpty()) {
      return prefix.suffixServer(captureAddress);
    } else {
      return server;
    }
  }

  public Set<FileInfo> getFilesList() {
    return filesList;
  }

  public List<Plugin> getPlugins() {
    return plugins;
  }

  public Configuration resolvePaths(PathResolver resolver, Flags flags) {
    return this;
  }

  public long getTestSuiteTimeout() {
    return testSuiteTimeout > 0 ? testSuiteTimeout : DefaultConfiguration.DEFAULT_TEST_TIMEOUT;
  }

  public List<FileInfo> getTests() {
    return tests;
  }

  public File getBasePath() {
    return basePath;
  }

  public JsonArray getGatewayConfiguration() {
    return gatewayConfig;
  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.config.Configuration#getDocType()
   */
  public DocType getDocType() {
    // TODO Auto-generated method stub
    return null;
  }
}
