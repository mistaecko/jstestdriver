// Copyright 2011 Google, Inc. All Rights Reserved.
package com.google.jstestdriver.server.gateway;

import com.google.common.collect.Iterators;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.jstestdriver.requesthandlers.HttpMethod;
import com.google.jstestdriver.requesthandlers.RequestHandler;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.mortbay.jetty.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * An HTTP gateway that forwards requests to another server and feeds the responses back to the
 * client.
 * TODO(rdionne): Write unit tests.
 * TODO(rdionne): Implement HOST header and 302 redirect host substitution.
 * @author rdionne@google.com (Robert Dionne)
 */
public class GatewayRequestHandler implements RequestHandler {

  public interface Factory {
    GatewayRequestHandler create(
        @Assisted("destination") String destination, @Assisted("prefix") String prefix);
  }

  private static final String HOST = "Host";
  private static final String LOCATION = "Location";
  private static final String PRAGMA = "Pragma";
  private static final String REQUEST_URI_DOES_NOT_START_WITH_PREFIX =
      "Request URI '%s' does not start with prefix '%s'.";
  private static final String X_SUPPRESS_STATUS_CODE = "X-Suppress-Status-Code";
  private static final String X_SUPPRESSED_STATUS_CODE = "X-Suppressed-Status-Code";
  private static final String X_SUPPRESSED_REASON_PHRASE = "X-Suppressed-Reason-Phrase";

  private final HttpClient client;
  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final String destination;
  private final String prefix;

  @Inject
  public GatewayRequestHandler(
      final HttpClient client, final HttpServletRequest req, final HttpServletResponse res,
      @Assisted("destination") String destination, @Assisted("prefix") String prefix) {
    this.client = client;
    this.request = req;
    this.response = res;
    this.destination = destination;
    this.prefix = prefix;
  }

  @Override
  public void handleIt() throws IOException {
    final HttpMethodBase method = getMethod(request);
    addRequestHeaders(method, request);
    method.setQueryString(request.getQueryString());
    spoofHostHeader(method);
    try {
      final int statusCode = client.executeMethod(method);
      response.setStatus(statusCode);
      addResponseHeaders(method, response);
      if (isRedirect(statusCode)) {
        spoofLocationHeader(request, (Response) response);
      }
      if (isStatusCodeSuppressed(request)) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.addIntHeader(X_SUPPRESSED_STATUS_CODE, statusCode);
        response.addHeader(X_SUPPRESSED_REASON_PHRASE, method.getStatusText());
      }
      // TODO(rdionne): Substitute the JsTD server address for the destination address in any redirects.
      Streams.copy(method.getResponseBodyAsStream(), response.getOutputStream());
    } catch (IOException e) {
      response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
      e.printStackTrace(response.getWriter());
    } finally {
      method.releaseConnection();
    }
  }

  private boolean isStatusCodeSuppressed(final HttpServletRequest request) {
    return Iterators.contains(
        Iterators.forEnumeration(request.getHeaders(PRAGMA)), X_SUPPRESS_STATUS_CODE);
  }

  private HttpMethodBase getMethod(final HttpServletRequest request) throws IOException {
    final HttpMethod method = HttpMethod.valueOf(request.getMethod());
    String uri = request.getRequestURI();
    if (prefix != null && !uri.startsWith(prefix)) {
      // Probably impossible.
      throw new RuntimeException(
          String.format(REQUEST_URI_DOES_NOT_START_WITH_PREFIX, uri, prefix));
    }
    String url = prefix == null ? destination + uri : destination + uri.substring(prefix.length());
    switch (method) {
      case POST:
      case PUT:
        return new GatewayEntityMethod(
            method.name(), url, request.getInputStream());
      default:
        return new GatewayMethod(method.name(), url);
    }
  }

  private void addRequestHeaders(final HttpMethodBase method, final HttpServletRequest request) {
    final Enumeration headers = request.getHeaderNames();
    while (headers.hasMoreElements()) {
      final String name = (String) headers.nextElement();
      final Enumeration values = request.getHeaders(name);
      while (values.hasMoreElements()) {
        final String value = (String) values.nextElement();
        method.addRequestHeader(name, value);
      }
    }
  }

  private void spoofHostHeader(HttpMethodBase method) {
    method.setRequestHeader(HOST, parseUri(destination).getAuthority());
  }

  private URI parseUri(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException badUriSyntax) {
      throw new RuntimeException(badUriSyntax);
    }
  }

  private void addResponseHeaders(final HttpMethodBase method, final HttpServletResponse response) {
    for (final Header header : method.getResponseHeaders()) {
      response.addHeader(header.getName(), header.getValue());
    }
  }

  private boolean isRedirect(int statusCode) {
    switch (statusCode) {
      case HttpServletResponse.SC_MOVED_PERMANENTLY:
      case HttpServletResponse.SC_FOUND:
      case HttpServletResponse.SC_SEE_OTHER:
      case HttpServletResponse.SC_TEMPORARY_REDIRECT:
        return true;
      default:
        return false;
    }
  }

  void spoofLocationHeader(HttpServletRequest request, Response response) {
    URI location = parseUri(response.getHeader(LOCATION));
    String host = request.getServerName();
    int port = request.getServerPort();
    URI destination = parseUri(this.destination);
    if (location.getHost() == null || !location.getHost().equals(destination.getHost())) {
      return;
    }
    if (location.getPort() == destination.getPort()
        || location.getPort() == -1 && (destination.getPort() == 80 || destination.getPort() == 443)) {
      response.setHeader(LOCATION,
          buildLocationHeader(location, host, port));
    }
  }

  private String buildLocationHeader(URI location, String host, int port) {
    try {
      return new URI(
          location.getScheme(),
          String.format("%s:%s", host, port),
          location.getPath(),
          location.getQuery(),
          location.getFragment()).toString();
    } catch (URISyntaxException badUriSyntax) {
      throw new RuntimeException(badUriSyntax);
    }
  }
}
