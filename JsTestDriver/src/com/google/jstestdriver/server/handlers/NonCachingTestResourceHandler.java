// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.server.handlers;

import com.google.inject.Inject;
import com.google.jstestdriver.requesthandlers.RequestHandler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Services test resource requests adding non-caching headers.
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class NonCachingTestResourceHandler implements RequestHandler {

  private static final String TIME_IN_THE_PAST = "Sat, 22 Sep 1984 00:00:00 GMT";

  private final TestResourceHandler handler;
  private final HttpServletRequest request;
  private final HttpServletResponse response;

  @Inject
  public NonCachingTestResourceHandler(TestResourceHandler handler,
      HttpServletRequest request,
      HttpServletResponse response) {
    this.handler = handler;
    this.request = request;
    this.response = response;
  }

  @Override
  public void handleIt() throws IOException {
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-Control", "private, no-cache, no-store, max-age=0, must-revalidate");
    response.setHeader("Expires", TIME_IN_THE_PAST);
    handler.handleIt();
  }
}
