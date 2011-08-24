// Copyright 2011 Google Inc. All Rights Reserved.
package com.google.jstestdriver.action;

import com.google.gson.JsonArray;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.google.jstestdriver.Action;
import com.google.jstestdriver.Server;
import com.google.jstestdriver.hooks.GatewayConfigurationFilter;
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.model.RunData;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Configures the gateway on the server by sending a POST application/jsonrequest
 * to /jstd/gateway with the gateway mappings.
 * @author rdionne@google.com (Robert Dionne)
 */
public class ConfigureGatewayAction implements Action {

  public interface Factory {
    ConfigureGatewayAction create(JsonArray gatewayConfig);
  }

  private final HandlerPathPrefix prefixer;
  private final String baseUrl;
  private final Server server;
  private final GatewayConfigurationFilter filter;
  private final JsonArray gatewayConfig;

  @Inject
  public ConfigureGatewayAction(
      @Named("serverHandlerPrefix") HandlerPathPrefix prefixer,
      @Named("server") String baseUrl,
      Server server,
      GatewayConfigurationFilter filter,
      @Assisted JsonArray gatewayConfig) {
    this.prefixer = prefixer;
    this.baseUrl = baseUrl;
    this.server = server;
    this.filter = filter;
    this.gatewayConfig = gatewayConfig;
  }

  public RunData run(RunData runData) {
    try {
      URL initialUrl = new URL(baseUrl + "/gateway");
      URL adjustedUrl = new URL(
          initialUrl.getProtocol(),
          initialUrl.getHost(),
          initialUrl.getPort(),
          prefixer.prefixPath(initialUrl.getPath(), "jstd"));
      server.postJson(adjustedUrl.toString(), filter.filter(gatewayConfig));
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return runData;
  }
}
