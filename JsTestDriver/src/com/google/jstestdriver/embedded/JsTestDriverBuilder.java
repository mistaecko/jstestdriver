// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.embedded;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.internal.Lists;
import com.google.jstestdriver.ActionRunner;
import com.google.jstestdriver.Args4jFlagsParser;
import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.PluginLoader;
import com.google.jstestdriver.config.CmdFlags;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.config.ConfigurationException;
import com.google.jstestdriver.config.ConfigurationSource;
import com.google.jstestdriver.config.InitializeModule;
import com.google.jstestdriver.config.Initializer;
import com.google.jstestdriver.config.UserConfigurationSource;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.hooks.PluginInitializer;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.output.TestResultListener;
import com.google.jstestdriver.runner.RunnerMode;

/**
 * @author corysmith@google.com (Your Name Here)
 *
 */
public class JsTestDriverBuilder {

  private File baseDir;
  private List<Module> pluginModules;
  private CmdFlags cmdLineFlags;
  private Configuration configuration;
  private final PluginLoader pluginLoader = new PluginLoader();
  private int port;
  private List<ServerListener> serverListeners = Lists.newArrayList();
  private List<TestResultListener> testListeners = Lists.newArrayList();
  private RunnerMode runnerMode;

  /**
   * @param absolutePath
   * @return
   */
  public JsTestDriverBuilder setConfiguration(String configPath) {
    setConfigurationSource(new UserConfigurationSource(new File(configPath)));
    return this;
  }
  
  /**
   * @param absolutePath
   * @return
   */
  public JsTestDriverBuilder setConfiguration(Configuration configuration) {
    this.configuration = configuration;
    return this;
  }

  /**
   * @param i
   * @return
   */
  public JsTestDriverBuilder setPort(int port) {
    this.port = port;
    // TODO Auto-generated method stub
    return this;
  }

  /**
   * @param testServerListener
   * @return
   */
  public JsTestDriverBuilder addServerListener(ServerListener testServerListener) {
    serverListeners.add(testServerListener);
    return this;
  }

  /**
   * 
   */
  public JsTestDriver build() {
    List<Module> initializeModules = Lists.newArrayList();
    File basePath;
    try {
      basePath = configuration.getBasePath().getCanonicalFile();
      initializeModules.add(
          new InitializeModule(
              pluginLoader,
              basePath,
              new Args4jFlagsParser(),
              runnerMode));
      Injector initializeInjector = Guice.createInjector(initializeModules);

      List<Module> actionRunnerModules;
      actionRunnerModules =
          initializeInjector.getInstance(Initializer.class).initialize(pluginModules,
              configuration, cmdLineFlags.getRunnerMode(), cmdLineFlags.getUnusedFlagsAsArgs());
      Injector injector = Guice.createInjector(actionRunnerModules);
      injector.getInstance(ActionRunner.class).runActions();
      return new JsTestDriver(injector);
    } catch (IOException e) {
      throw new ConfigurationException("failure during build", e);
    }
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
  public JsTestDriverBuilder setRunnerMode(RunnerMode runnerMode) {
    this.runnerMode = runnerMode;
    return this;
  }

  /**
   * @param testTestResultsListener
   * @return
   */
  public JsTestDriverBuilder addTestListener(TestResultListener testResultListener) {
    testListeners.add(testResultListener);
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
    this.baseDir = file;
    return this;
  }

  /**
   * @param configurationSource
   */
  public JsTestDriverBuilder setConfigurationSource(ConfigurationSource source) {
    setConfiguration(source.parse(baseDir, new YamlParser()));
    return this;
  }

  /**
   * Modules for the plugins.
   * @param pluginModules
   * @return 
   * @deprecated
   */
  public JsTestDriverBuilder addPluginModules(List<Module> pluginModules) {
    this.pluginModules = pluginModules;
    return this;
  }

  /**
   * Don't pass in the commandline flags.
   * @param cmdLineFlags
   * @return 
   * @deprecated
   */
  // TODO(corysmith): Move the error handling out of the Args4jFlagsParser.
  public JsTestDriverBuilder setCmdLineFlags(CmdFlags cmdLineFlags) {
    this.cmdLineFlags = cmdLineFlags;
    return this;
  }

}
