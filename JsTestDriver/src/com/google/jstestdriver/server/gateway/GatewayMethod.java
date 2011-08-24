// Copyright 2011 Google, Inc. All Rights Reserved.
package com.google.jstestdriver.server.gateway;

import org.apache.commons.httpclient.HttpMethodBase;

/**
 * A chameleon {@link HttpMethodBase}.
 * @author rdionne@google.com (Robert Dionne)
 */
class GatewayMethod extends HttpMethodBase {

  private final String name;

  GatewayMethod(final String name, final String uri) {
    super(uri);
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }
}
