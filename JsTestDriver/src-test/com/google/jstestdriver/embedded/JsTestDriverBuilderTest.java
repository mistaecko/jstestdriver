// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.embedded;

import com.google.inject.Module;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.FileResult;
import com.google.jstestdriver.Flags;
import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.TestResult;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.hooks.PluginInitializer;
import com.google.jstestdriver.output.TestResultListener;
import com.google.jstestdriver.runner.RunnerMode;
import com.google.jstestdriver.hooks.ServerListener;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileWriter;

/**
 * @author corysmith@google.com (Your Name Here)
 *
 */
public class JsTestDriverBuilderTest extends TestCase {
  private File tmpDir;

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
  
  private static final class TestTestResultsListener implements TestResultListener {

    public void onTestComplete(TestResult testResult) {
      // TODO Auto-generated method stub
      
    }

    public void onFileLoad(String browser, FileResult fileResult) {
      // TODO Auto-generated method stub
      
    }

    public void finish() {
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
      .addServerListener(new TestServerListener())
      .build();
  }

  public void testBuildClient() throws Exception {
    File configuration = new File(tmpDir, "config.yml");
    FileWriter writer = new FileWriter(configuration);
    writer.append("");
    writer.flush();
    JsTestDriver client = new JsTestDriverBuilder()
      .setConfiguration(configuration.getAbsolutePath())
      .setServer("http://localhost:8080")
      .withPluginInitializer(TestInitializer.class)
      .addTestListener(new TestTestResultsListener())
      .build();
  }
}
