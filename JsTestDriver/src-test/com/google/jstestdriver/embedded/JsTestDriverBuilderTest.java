// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.embedded;

import com.google.inject.Module;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.Flags;
import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.hooks.PluginInitializer;
import com.google.jstestdriver.runner.RunnerMode;
import com.google.jstestdriver.server.ServerListener;

import junit.framework.TestCase;

import java.io.File;

/**
 * @author corysmith@google.com (Your Name Here)
 *
 */
public class JsTestDriverBuilderTest extends TestCase {
  private File tmpDir;

  /**
   * @author corysmith@google.com (Your Name Here)
   *
   */
  private final class TestServerListener implements ServerListener {
    public void serverStopped() {
      // TODO Auto-generated method stub
      
    }

    public void serverStarted() {
      // TODO Auto-generated method stub
      
    }

    public void browserPanicked(BrowserInfo info) {
      // TODO Auto-generated method stub
      
    }

    public void browserCaptured(BrowserInfo info) {
      // TODO Auto-generated method stub
      
    }
  }

  @Override
  protected void setUp() throws Exception {
    tmpDir = new File(this.toString());
    tmpDir.mkdirs();
    tmpDir.deleteOnExit();
  }

  @Override
  protected void tearDown() throws Exception {
    tmpDir.delete();
  }
  
  private static class TestInitializer implements PluginInitializer {
    public Module initializeModule(Flags flags, Configuration config) {
      return null;
    }
  }
  

  public void testBuildServer() throws Exception {
    File configuration = new File(tmpDir, "config.yml");
    JsTestDriver server = new JsTestDriverBuilder()
      .setConfiguration(configuration.getAbsolutePath())
      .setRunnerMode(RunnerMode.QUIET)
      .setPort(8080)
      .addListener(new TestServerListener())
      .build();
  }

  public void testBuildClient() throws Exception {
    File configuration = new File(tmpDir, "config.yml");
    JsTestDriver client = new JsTestDriverBuilder()
      .setConfiguration(configuration.getAbsolutePath())
      .setPort(8080)
      .withPluginInitializer(TestInitializer.class)
      .addListener(new TestServerListener())
      .build();
  }
}
