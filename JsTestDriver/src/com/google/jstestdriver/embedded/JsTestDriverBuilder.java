// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.embedded;

import java.io.File;

import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.hooks.PluginInitializer;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.output.TestResultListener;
import com.google.jstestdriver.runner.RunnerMode;

/**
 * @author corysmith@google.com (Your Name Here)
 *
 */
public class JsTestDriverBuilder {

  /**
   * @param absolutePath
   * @return
   */
  public JsTestDriverBuilder setConfiguration(String absolutePath) {
    // TODO Auto-generated method stub
    return this;
  }

  /**
   * @param i
   * @return
   */
  public JsTestDriverBuilder setPort(int i) {
    // TODO Auto-generated method stub
    return this;
  }

  /**
   * @param testServerListener
   * @return
   */
  public JsTestDriverBuilder addServerListener(ServerListener testServerListener) {
    // TODO Auto-generated method stub
    return this;
  }

  /**
   * 
   */
  public JsTestDriver build() {
    return null;
  }

  /**
   * @param plugin
   * @return
   */
  public JsTestDriverBuilder withPluginInitializer(
        Class<? extends PluginInitializer> initializer) {
    return this;
  }

  /**
   * @param mode
   * @return
   */
  public JsTestDriverBuilder setRunnerMode(RunnerMode mode) {
    return this;
  }

  /**
   * @param testTestResultsListener
   * @return
   */
  public JsTestDriverBuilder addTestListener(TestResultListener testResultListener) {
    return this;
  }

  /**
   * @param string
   * @return
   */
  public JsTestDriverBuilder setServer(String serverAddress) {
    return this;
  }

  /**
   * @param file
   * @return
   */
  public JsTestDriverBuilder setBaseDir(File file) {
    return this;
  }

}
