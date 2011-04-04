// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.embedded;

import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.Plugin;
import com.google.jstestdriver.hooks.PluginInitializer;
import com.google.jstestdriver.runner.RunnerMode;
import com.google.jstestdriver.server.ServerListener;

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
  public JsTestDriverBuilder addListener(ServerListener testServerListener) {
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
  public JsTestDriverBuilder withPluginInitializer(Class<? extends PluginInitializer> initializer) {
    return this;
  }

  /**
   * @param mode
   * @return
   */
  public JsTestDriverBuilder setRunnerMode(RunnerMode mode) {
    // TODO Auto-generated method stub
    return this;
  }

}
