// Copyright 2011 Google Inc. All Rights Reserved.
package com.google.jstestdriver.server.proxy;

/**
 * Enum describing proxy/gateway behavior.
 * TODO(rdionne): Eliminate all references to {@link ProxyBehavior}.
 * @deprecated Just use {@link GatewayServlet}.
 * @author rdionne@google.com (Robert Dionne)
 */
public enum ProxyBehavior {

  /**
   * Forward the HOST header.
   */
  FORWARD,

  /**
   * Do The Right Thing(TM).
   */
  GATEWAY,

  /**
   * Spoof the HOST header.
   */
  SPOOF
}
