// Copyright 2011 Google, Inc. All Rights Reserved.
package com.google.jstestdriver.server.proxy;

import com.google.inject.Inject;
import com.google.jstestdriver.requesthandlers.HttpMethod;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;

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

/**
 * An HTTP gateway that forwards requests to another server and feeds the responses back to the
 * client.
 * TODO(rdionne): Write unit tests.
 * TODO(rdionne): Implement HOST header and 302 redirect host substitution.
 * @author rdionne@google.com (Robert Dionne)
 */
public class GatewayServlet extends JstdProxyServlet {

  private final HttpClient client;
  private String proxyTo;
  private String prefix;

  @Inject
  public GatewayServlet(final HttpClient client) {
    this.client = client;
  }

  @Override
  public void init(final ServletConfig servletConfig) throws ServletException {
    proxyTo = servletConfig.getInitParameter("ProxyTo");
    prefix = servletConfig.getInitParameter("Prefix");
  }

  @Override
  public ServletConfig getServletConfig() {
    throw new UnsupportedOperationException("#getSerlvetConfig() not supported.");
  }

  @Override
  public void service(final ServletRequest req, final ServletResponse res)
        throws ServletException, IOException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpMethodBase method = getMethod(request);
    addRequestHeaders(method, request);
    method.setQueryString(request.getQueryString());
    try {
      method.setRequestHeader("Host", new URI(proxyTo).getAuthority());
    } catch (URISyntaxException badUriSyntax) {
      throw new RuntimeException(badUriSyntax);
    }
    final int statusCode = client.executeMethod(method);
    final HttpServletResponse response = (HttpServletResponse) res;
    response.setStatus(statusCode);
    addResponseHeaders(method, response);
    // TODO(rdionne): Substitute the JsTD server address for the proxyTo address in any redirects.
    Streams.copy(method.getResponseBodyAsStream(), response.getOutputStream());
    method.releaseConnection();
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

  private void addResponseHeaders(final HttpMethodBase method, final HttpServletResponse response) {
    for (final Header header : method.getResponseHeaders()) {
      response.addHeader(header.getName(), header.getValue());
    }
  }

  private HttpMethodBase getMethod(final HttpServletRequest request) throws IOException {
    final HttpMethod method = HttpMethod.valueOf(request.getMethod());
    String uri = request.getRequestURI();
    if (prefix != null && !uri.startsWith(prefix)) {
      throw new RuntimeException("Request URI " + uri + " does not start with prefix " + prefix);
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

  @Override
  public String getServletInfo() {
    throw new UnsupportedOperationException("#getServletInfo() not supported.");
  }

  @Override
  public void destroy() {
    throw new UnsupportedOperationException("#destroy() not supported.");
  }
}
