// Copyright 2011 Google Inc. All Rights Reserved.
package com.google.jstestdriver.server.gateway;

import junit.framework.TestCase;

import org.apache.commons.httpclient.methods.EntityEnclosingMethod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author rdionne@google.com (Robert Dionne)
 */
public class GatewayEntityMethodTest extends TestCase {

  public void testGetNameGetUriAndGetRequestEntity() throws Exception {
    ByteArrayInputStream in = new ByteArrayInputStream("ASDF".getBytes());
    EntityEnclosingMethod method = new GatewayEntityMethod("POST", "http://www.google.com/search", in);
    assertEquals("POST", method.getName());
    assertEquals("http://www.google.com/search", method.getURI().toString());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    method.getRequestEntity().writeRequest(out);
    assertEquals("ASDF", out.toString());
  }
}
