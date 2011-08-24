// Copyright 2011 Google, Inc. All Rights Reserved.
package com.google.jstestdriver.server.gateway;

import org.apache.commons.httpclient.methods.EntityEnclosingMethod;

import java.io.InputStream;

/**
 * An {@link EntityEnclosingMethod} that streams the given {@link InputStream} as the request body.
 * @author rdionne@google.com (Robert Dionne)
 */
class GatewayEntityMethod extends EntityEnclosingMethod {

  private final String name;

  GatewayEntityMethod(final String name, final String uri, final InputStream entityBody) {
    super(uri);
    this.name = name;
    this.setRequestBody(entityBody);
  }

  @Override
  public String getName() {
    return name;
  }
}
