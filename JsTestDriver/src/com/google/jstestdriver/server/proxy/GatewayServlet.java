// Copyright 2011 Google, Inc. All Rights Reserved.
package com.google.jstestdriver.server.proxy;

import com.google.inject.Inject;
import com.google.jstestdriver.requesthandlers.HttpMethod;


import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.mortbay.jetty.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Enumeration;


/**
 * An HTTP gateway that forwards requests to another server and feeds the responses back to the
 * client.
 * TODO(rdionne): Write unit tests.
 * TODO(rdionne): Implement HOST header and 302 redirect host substitution.
 * @author rdionne@google.com (Robert Dionne)
 */
public class GatewayServlet extends JstdProxyServlet {

  private static final String HOST = "Host";
  private static final String LOCATION = "Location";
  private static final String DESTROY_NOT_SUPPORTED = "#destroy() not supported.";
  private static final String GET_SERLVET_CONFIG_NOT_SUPPORTED =
      "#getSerlvetConfig() not supported.";
  private static final String GET_SERVLET_INFO_NOT_SUPPORTED = "#getServletInfo() not supported.";
  private static final String REQUEST_URI_DOES_NOT_START_WITH_PREFIX =
      "Request URI '%s' does not start with prefix '%s'.";

  private final HttpClient client;
  private String proxyTo;
  private String prefix;

  @Inject
  public GatewayServlet(final HttpClient client) {
    this.client = client;
  }

  @Override
  public void init(final ServletConfig servletConfig) throws ServletException {
    proxyTo = servletConfig.getInitParameter(ProxyServletConfig.PROXY_TO);
    prefix = servletConfig.getInitParameter(ProxyServletConfig.PREFIX);
  }

  @Override
  public ServletConfig getServletConfig() {
    throw new UnsupportedOperationException(GET_SERLVET_CONFIG_NOT_SUPPORTED);
  }

  @Override
  public void service(final ServletRequest req, final ServletResponse res)
        throws ServletException, IOException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpMethodBase method = getMethod(request);
    addRequestHeaders(method, request);
    method.setQueryString(request.getQueryString());
    spoofHostHeader(method);
    final int statusCode = client.executeMethod(method);
    final HttpServletResponse response = (HttpServletResponse) res;
    response.setStatus(statusCode);
    addResponseHeaders(method, response);
    if (isRedirect(statusCode)) {
      spoofLocationHeader(request, (Response) response);
    }
    // TODO(rdionne): Substitute the JsTD server address for the proxyTo address in any redirects.
    Streams.copy(method.getResponseBodyAsStream(), response.getOutputStream());
    method.releaseConnection();
  }

  private HttpMethodBase getMethod(final HttpServletRequest request) throws IOException {
    final HttpMethod method = HttpMethod.valueOf(request.getMethod());
    String uri = request.getRequestURI();
    if (prefix != null && !uri.startsWith(prefix)) {
      // Probably impossible.
      throw new RuntimeException(
          String.format(REQUEST_URI_DOES_NOT_START_WITH_PREFIX, uri, prefix));
    }
    String url = prefix == null ? proxyTo + uri : proxyTo + uri.substring(prefix.length());
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
    method.setRequestHeader(HOST, parseUri(proxyTo).getAuthority());
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
    URI proxyTo = parseUri(this.proxyTo);
    if (location.getHost() == null || !location.getHost().equals(proxyTo.getHost())) {
      return;
    }
    if (location.getPort() == proxyTo.getPort()
        || location.getPort() == -1 && (proxyTo.getPort() == 80 || proxyTo.getPort() == 443)) {
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

  @Override
  public String getServletInfo() {
    throw new UnsupportedOperationException(GET_SERVLET_INFO_NOT_SUPPORTED);
  }

  @Override
  public void destroy() {
    throw new UnsupportedOperationException(DESTROY_NOT_SUPPORTED);
  }
}
