// Copyright 2011 Google Inc. All Rights Reserved.
package com.google.jstestdriver.server.proxy;

import org.mortbay.servlet.ProxyServlet;

/**
 * A {@link Transparent} that we could reconfigure to
 * intercept various headers.
 * @author rdionne@google.com (Robert Dionne)
 */
public class HostSpoofingJstdProxyServlet extends JstdProxyServlet {
  {_DontProxyHeaders.add("host");}
}
