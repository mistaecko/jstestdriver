// Copyright 2011 Google Inc. All Rights Reserved.
package com.google.jstestdriver.requesthandlers;

import static com.google.jstestdriver.requesthandlers.HttpMethod.ANY;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.jstestdriver.server.gateway.GatewayRequestHandler;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import javax.servlet.ServletException;

/**
 * @author rdionne@google.com (Robert Dionne)
 */
public class GatewayConfigurationTest extends TestCase {

  private static final String CONFIG = "[" +
      "{\"matcher\":\"/asdf\",\"server\":\"http://www.asdf.com\"}" +
  "]";

  private IMocksControl control;

  private GatewayRequestHandler gateway;
  private GatewayRequestHandler.Factory gatewayProvider;

  private GatewayConfiguration configuration;

  @Override
  protected void setUp() throws Exception {
    control = EasyMock.createControl();
    gateway = control.createMock(GatewayRequestHandler.class);
    gatewayProvider = control.createMock(GatewayRequestHandler.Factory.class);
    configuration = new GatewayConfiguration(gatewayProvider);
  }

  public void testEmptyUponInitialization() {
    assertEquals(0, configuration.getMatchers().size());
  }

  public void testUpdateConfiguration() throws ServletException {
    EasyMock.expect(gatewayProvider.create("http://www.asdf.com", "/asdf")).andReturn(
        gateway);
    control.replay();
    JsonArray jsonConfig = new JsonParser().parse(CONFIG).getAsJsonArray();
    configuration.updateConfiguration(jsonConfig);
    assertEquals(1, configuration.getMatchers().size());
    assertEquals(gateway,
        configuration.getRequestHandler(new RequestMatcher(ANY, "/asdf")));
    control.verify();
  }
}
