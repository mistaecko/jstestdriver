// Copyright 2011 Google Inc. All Rights Reserved.
package com.google.jstestdriver.hooks;

import com.google.gson.JsonArray;
import com.google.inject.ImplementedBy;
import com.google.jstestdriver.requesthandlers.DefaultGatewayConfigurationFilter;

/**
 * @author rdionne@google.com (Robert Dionne)
 */
@ImplementedBy(DefaultGatewayConfigurationFilter.class)
public interface GatewayConfigurationFilter {

  JsonArray filter(JsonArray gatewayConfig);
}
