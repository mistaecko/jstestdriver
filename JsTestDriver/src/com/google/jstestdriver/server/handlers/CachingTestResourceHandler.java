// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.server.handlers;

import com.google.inject.Inject;
import com.google.jstestdriver.requesthandlers.RequestHandler;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Services test resource requests adding caching headers.
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class CachingTestResourceHandler implements RequestHandler {
  private static final Logger logger = LoggerFactory.getLogger(CachingTestResourceHandler.class);

  private static final DateFormat RFC1123_PATTERN =
      new SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z", Locale.US);

  private final TestResourceHandler handler;
  private final HttpServletRequest request;
  private final HttpServletResponse response;

  @Inject
  public CachingTestResourceHandler(TestResourceHandler handler,
      HttpServletRequest request,
      HttpServletResponse response) {
    this.handler = handler;
    this.request = request;
    this.response = response;
  }

  @Override
  public void handleIt() throws IOException {
    logger.info("handinling {} with headers {}", request.getPathInfo().substring(1), Collections.list(request.getHeaderNames()));
    // tests requesting the file in short succession
    response.setHeader("Cache-Control", "max-age=600");
    response.setHeader("Last-Modified",
        RFC1123_PATTERN.format(new DateTime().minusMinutes(1).toDate()));
    handler.handleIt();
  }
}
