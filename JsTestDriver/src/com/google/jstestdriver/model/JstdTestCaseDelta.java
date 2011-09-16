// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.model;

import com.google.jstestdriver.FileInfo;

import java.util.List;

/**
 * Represents the changed files in a JstdTestCase.
 * 
 * @author corysmith@google.com (Cory Smith)
 */
public class JstdTestCaseDelta {

  private final List<FileInfo> dependencies;
  private final List<FileInfo> tests;
  private final List<FileInfo> plugins;

  public JstdTestCaseDelta(List<FileInfo> dependencies, List<FileInfo> tests, List<FileInfo> plugins) {
    this.dependencies = dependencies;
    this.tests = tests;
    this.plugins = plugins;
  }

  public List<FileInfo> getTests() {
    return tests;
  }

  public List<FileInfo> getDependencies() {
    return dependencies;
  }

  public List<FileInfo> getPlugins() {
    return plugins;
  }
}
