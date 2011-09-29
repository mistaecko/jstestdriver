// Copyright 2011 Google Inc. All Rights Reserved.
package com.google.jstestdriver.requesthandlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.jstestdriver.hooks.GatewayConfigurationFilter;
import com.google.jstestdriver.hooks.ProxyDestination;

/**
 * @author rdionne@google.com (Robert Dionne)
 */
public class DefaultGatewayConfigurationFilter implements
    GatewayConfigurationFilter {
  
  @Inject(optional=true) private ProxyDestination destination;

  public JsonArray filter(JsonArray gatewayConfig) {
    if (destination == null) {
      return gatewayConfig;
    } else {
      final JsonArray newGatewayConfig = new JsonArray();
      newGatewayConfig.addAll(gatewayConfig);
      final JsonObject entry = new JsonObject();
      entry.addProperty("matcher", "*");
      entry.addProperty("server", destination.getDestinationAddress());
      newGatewayConfig.add(entry);
      return newGatewayConfig;
    }
  }
}
