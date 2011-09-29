// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.embedded;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.PluginLoader;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.config.ConfigurationSource;
import com.google.jstestdriver.config.UserConfigurationSource;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.hooks.PluginInitializer;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.output.TestResultListener;
import com.google.jstestdriver.runner.RunnerMode;

import java.io.File;
import java.util.List;

/**
 * @author corbinrsmith@gmail.com (Cory Smith)
 *
 */
public class JsTestDriverBuilder {

  private File baseDir;
  private List<Module> pluginModules = Lists.newArrayList();
  private String[] flags = new String[]{};
  private Configuration configuration;
  private final PluginLoader pluginLoader = new PluginLoader();
  private int port = -1;
  private final List<ServerListener> serverListeners = Lists.newArrayList();
  private final List<TestResultListener> testListeners = Lists.newArrayList();
  private RunnerMode runnerMode = RunnerMode.QUIET;
  private String serverAddress;
  final private List<Class<? extends PluginInitializer>> pluginInitializers =  Lists.newArrayList();


  /**
   * @param configPath
   * @return The builder.
   */
  public JsTestDriverBuilder setDefaultConfiguration(String configPath) {

    setConfigurationSource(new UserConfigurationSource(new File(configPath)));
    return this;
  }
  
  /**
   * @param configuration
   * @return The builder.
   */
  public JsTestDriverBuilder setDefaultConfiguration(Configuration configuration) {
    this.configuration = configuration;
    return this;
  }

  /**
   * @param port
   * @return The builder.
   */
  public JsTestDriverBuilder setPort(int port) {
    this.port = port;
    return this;
  }

  /**
   * @param testServerListener
   * @return The builder.
   */
  public JsTestDriverBuilder addServerListener(ServerListener testServerListener) {
    serverListeners.add(testServerListener);
    return this;
  }


  /**
   * 
   */
  public JsTestDriver build() {
    // TODO(corysmith): add check to resolve the serverAddress and port issues.
    List<Module> plugins = Lists.newArrayList(pluginModules);
    plugins.add(new ListenerBindingModule(serverListeners, testListeners));
    return new JsTestDriver(configuration,
        pluginLoader,
        pluginInitializers,
        runnerMode,
        flags,
        port,
        plugins,
        serverListeners,
        testListeners,
        baseDir,
        serverAddress);
  }

  /**
   * @param plugin
   * @return
   */
  public JsTestDriverBuilder withPluginInitializer(
        Class<? extends PluginInitializer> initializer) {
    pluginInitializers.add(initializer);
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
    this.serverAddress = serverAddress;
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
    setDefaultConfiguration(source.parse(baseDir, new YamlParser()));
    return this;
  }

  /**
   * Modules for the plugins.
   * @param pluginModules
   * @return 
   * @deprecated In favor of passing in {@link PluginInitializer}s
   */
  public JsTestDriverBuilder addPluginModules(List<Module> pluginModules) {
    this.pluginModules = pluginModules;

    return this;
  }

  /**
   * Don't pass in the commandline flags.
   * @param flags
   * @return 
   * @deprecated
   */
  public JsTestDriverBuilder setFlags(String[] flags) {
    this.flags = flags;
    return this;
  }

  private static class ListenerBindingModule implements Module {
    private final List<? extends ServerListener> serverListeners;
    private final List<? extends TestResultListener>testListeners;
    ListenerBindingModule(List<? extends ServerListener> serverListeners,
        List<? extends TestResultListener> testListeners) {
      this.serverListeners = serverListeners;
      this.testListeners = testListeners;
    }

    @Override
    public void configure(Binder binder) {
      Multibinder<ServerListener> serverSetBinder =
          Multibinder.newSetBinder(binder, ServerListener.class);
      for (ServerListener listener : serverListeners) {
        serverSetBinder.addBinding().toInstance(listener);
      }
      Multibinder<TestResultListener> testSetBinder =
        Multibinder.newSetBinder(binder, TestResultListener.class);
      for (TestResultListener listener : testListeners) {
        testSetBinder.addBinding().toInstance(listener);
      }
    }
  }
  
}
