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
package com.google.jstestdriver;

import com.google.common.collect.Sets;
import com.google.jstestdriver.browser.BrowserRunner;
import com.google.jstestdriver.browser.CommandLineBrowserRunner;
import com.google.jstestdriver.config.ConfigurationSource;
import com.google.jstestdriver.config.DefaultConfigurationSource;
import com.google.jstestdriver.config.ExecutionType;
import com.google.jstestdriver.config.UserConfigurationSource;
import com.google.jstestdriver.model.ConcretePathPrefix;
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.runner.RunnerMode;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * FlagsParser for the JsTestDriver.
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
/**
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class FlagsImpl implements Flags {

  private Integer port = -1;
  private Integer sslPort = -1;
  private String server;
  private String captureAddress;
  private String testOutput = "";
  private Set<BrowserRunner> browser = Sets.newHashSet();
  private boolean reset;
  private long browserTimeout = SlaveBrowser.TIMEOUT;
  private ConfigurationSource config = new DefaultConfigurationSource();
  private List<String> tests = new ArrayList<String>();
  private boolean displayHelp = false;
  private boolean verbose = false;
  private boolean captureConsole = false;
  private boolean preloadFiles = false;
  private List<String> dryRunFor = new ArrayList<String>();
  @Argument
  private List<String> arguments = new ArrayList<String>();
  private RunnerMode runnerMode = RunnerMode.QUIET;
  private HashSet<String> requiredBrowsers;
  private HandlerPathPrefix serverHandlerPrefix = new NullPathPrefix();

  @Option(name="--port", usage="The port on which to start the JsTestDriver server")
  public void setPort(Integer port) {
    this.port = port;
  }

  @Override
  public Integer getPort() {
    return port;
  }

  @Option(name="--sslPort", usage="The SSL port on which to start the JsTestDriver server")
  public void setSslPort(Integer sslPort) {
    this.sslPort = sslPort;
  }

  @Override
  public Integer getSslPort() {
    return sslPort;
  }

  @Option(name="--server", usage="The server to which to send the command")
  public void setServer(String server) {
    this.server = server;
  }

  @Override
  public String getServer() {
    return server;
  }

  @Option(name="--captureAddress", usage="The address to capture the browser.")
  public void setCaptureAddress(String captureAddress) {
    this.captureAddress = captureAddress;
  }

  @Override
  public String getCaptureAddress() {
    return captureAddress;
  }

  @Override
  public List<String> getArguments() {
    return arguments;
  }

  @Option(name="--testOutput", usage="A directory to which serialize the results of the tests as" +
      " XML")
  public void setTestOutput(String testOutput) {
    this.testOutput = testOutput;
  }

  @Override
  public String getTestOutput() {
    return testOutput;
  }

  @Option(name="--browser", usage="The path to the browser executable")
  public void setBrowser(List<String> browsers) {
    for (String browser : browsers) {
      String[] splitBrowser = browser.split(";", 2);
      String browserArgs = "";
      if (splitBrowser.length == 2) {
        browserArgs = splitBrowser[1].replace(";", " ");
        System.out.println(browserArgs);
      }
      this.browser.add(
          new CommandLineBrowserRunner(splitBrowser[0], browserArgs, new SimpleProcessFactory()));
    }
  }

  @Override
  public Set<BrowserRunner> getBrowser() {
    return browser;
  }

  @Option(name="--reset", usage="Resets the runner")
  public void setReset(boolean reset) {
    this.reset = reset;
  }

  @Override
  public boolean getReset() {
    return reset;
  }

  @Option(name="--config", usage="Loads the configuration file")
  public void setConfig(String config) {
    this.config = new UserConfigurationSource(new File(config).getAbsoluteFile());
  }

  @Override
  public ConfigurationSource getConfig() {
    return config;
  }

  @Option(name="--tests",
          usage="Run the tests specified in the form testCase.testName")
  public void setTests(List<String> tests) {
    this.tests = tests;
  }

  @Override
  public List<String> getTests() {
    return tests;
  }

  @Option(name="--help", usage="Help")
  public void setDisplayHelp(boolean displayHelp) {
    this.displayHelp = displayHelp;
  }
  
  @Override
  public boolean getDisplayHelp() {
    return displayHelp;
  }

  @Option(name="--verbose", usage="Displays more information during a run")
  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  @Override
  public boolean getVerbose() {
    return verbose;
  }

  @Option(name="--captureConsole", usage="Capture the console (if possible) from the browser")
  public void setCaptureConsole(boolean captureConsole) {
    this.captureConsole = captureConsole;
  }

  @Override
  public boolean getCaptureConsole() {
    return captureConsole;
  }

  @Option(name="--preloadFiles", usage="Preload the js files")
  public void setPreloadFiles(boolean preloadFiles) {
    this.preloadFiles = preloadFiles;
  }

  @Override
  public boolean getPreloadFiles() {
    return preloadFiles;
  }

  @Option(name="--dryRunFor", usage="Outputs the number of tests that are going to be run as well" +
          " as their names for a set of expressions or all to see all the tests")
  public void setDryRunFor(List<String> dryRunFor) {
    this.dryRunFor = dryRunFor;
  }

  @Override
  public List<String> getDryRunFor() {
    return dryRunFor;
  }

  public RunnerMode getRunnerMode() {
    return runnerMode;
  }
  
  @Option(name="--browserTimeout", usage="The ms before a browser is declared dead.")
  public void setBrowserTimeout(Long browserTimeout) {
    this.browserTimeout = browserTimeout;
  }

  @Override
  public long getBrowserTimeout() {
    return browserTimeout;
  }

  @Option(name="--requiredBrowsers",
      usage="Browsers that all actions must be run on.")
  public void setRequiredBrowsers(List<String> requiredBrowsers) {
    this.requiredBrowsers = Sets.newHashSet(requiredBrowsers);
  }
  
  @Override
  public Set<String> getRequiredBrowsers() {
    return requiredBrowsers;
  }

  @Option(name="--serverHandlerPrefix",
      usage="Whether the handlers will be prefixed with jstd")
  public void setServerHandlerPrefix(String serverHandlerPrefix) {
    this.serverHandlerPrefix = new ConcretePathPrefix(serverHandlerPrefix.startsWith("/") ?
        serverHandlerPrefix.substring(1) : serverHandlerPrefix);
  }

  @Override
  public HandlerPathPrefix getServerHandlerPrefix() {
    return serverHandlerPrefix;
  }
  
  

  @Override
  public String toString() {
    return "FlagsImpl [port=" + port + ",\n sslPort=" + sslPort + ",\n server=" + server
        + ",\n testOutput=" + testOutput + ",\n browser=" + browser + ",\n reset=" + reset
        + ",\n browserTimeout=" + browserTimeout + ",\n config=" + config + ",\n tests=" + tests
        + ",\n displayHelp=" + displayHelp + ",\n verbose=" + verbose + ",\n captureConsole="
        + captureConsole + ",\n preloadFiles=" + preloadFiles + ",\n dryRunFor=" + dryRunFor
        + ",\n arguments=" + arguments + ",\n runnerMode=" + runnerMode + ",\n requiredBrowsers="
        + requiredBrowsers + ",\n serverHandlerPrefix=" + serverHandlerPrefix +  "]";
  }

  @Override
  public ExecutionType getExecutionType() {
    return ExecutionType.INTERACTIVE;
  }
}
