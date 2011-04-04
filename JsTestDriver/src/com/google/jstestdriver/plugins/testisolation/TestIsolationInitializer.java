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
package com.google.jstestdriver.plugins.testisolation;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.jstestdriver.Flags;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.hooks.JstdTestCaseProcessor;
import com.google.jstestdriver.hooks.PluginInitializer;

/**
 * Initializer to configure test isolation.
 * 
 * @author Andrew Trenk
 */
public class TestIsolationInitializer implements PluginInitializer {

  public Module initializeModule(Flags flags, Configuration config) {    
    return new TestIsolationModule();
  }
  
  private static class TestIsolationModule extends AbstractModule {
    @Override
    protected void configure() {       
      Multibinder.newSetBinder(binder(), JstdTestCaseProcessor.class)
          .addBinding().to(IsolationTestCaseProcessor.class);
    }
  }
}