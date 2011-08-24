// Copyright 2011 Google Inc. All Rights Reserved.
package com.google.jstestdriver.server.gateway;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpMethod;

/**
 * @author rdionne@google.com (Robert Dionne)
 */
public class GatewayMethodTest extends TestCase {

  public void testGetNameAndGetUri() throws Exception {
    HttpMethod method = new GatewayMethod("GET", "http://www.google.com/search");
    assertEquals("GET", method.getName());
    assertEquals("http://www.google.com/search", method.getURI().toString());
  }
}
