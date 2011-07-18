// Copyright 2011 Google Inc. All Rights Reserved.
package com.google.jstestdriver.server.proxy;

import static org.easymock.EasyMock.expect;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author rdionne@google.com (Robert Dionne)
 */
public class GatewayServletTest extends TestCase {

  private IMocksControl control;
  private HttpClient client;
  private GatewayServlet gateway;
  private ServletConfig config;
  private HttpServletRequest request;
  private HttpServletResponse response;

  @Override
  protected void setUp() throws Exception {
    control = EasyMock.createControl();
    client = control.createMock(HttpClient.class);
    gateway = new GatewayServlet(client);
    config = control.createMock(ServletConfig.class);
    request = control.createMock(HttpServletRequest.class);
    response = control.createMock(HttpServletResponse.class);
  }

  @Override
  protected void tearDown() throws Exception {
    control.verify();
  }

  public void testService_GET() throws Exception {
    expect(config.getInitParameter("ProxyTo")).andStubReturn("http://hostname:80");
    expect(request.getMethod()).andStubReturn("GET");
    expect(request.getRequestURI()).andStubReturn("/relativeUri");
    expect(request.getHeaderNames()).andStubReturn(
        Iterators.asEnumeration(ImmutableList.of("Host").iterator()));
    expect(request.getHeaders("Host")).andStubReturn(
        Iterators.asEnumeration(ImmutableList.of("jstd:80").iterator()));
    expect(request.getQueryString()).andStubReturn("?id=123");
    expect(client.executeMethod(EasyMock.<HttpMethodBase>anyObject())).andStubReturn(200);
    /* expect */ response.setStatus(200);
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    ServletOutputStream out = new ServletOutputStream() {
      @Override
      public void write(int b) throws IOException {
        output.write(b);
      }
    };
    expect(response.getOutputStream()).andStubReturn(out);
    control.replay();
    gateway.init(config);
    gateway.service(request, response);
    assertEquals("", output.toString());
  }

  public void testService_POST() throws Exception {
    expect(config.getInitParameter("ProxyTo")).andStubReturn("http://hostname:80");
    expect(request.getMethod()).andStubReturn("POST");
    expect(request.getRequestURI()).andStubReturn("/relativeUri");
    final ByteArrayInputStream input = new ByteArrayInputStream("ASDF".getBytes());
    ServletInputStream in = new ServletInputStream() {
      @Override
      public int read() throws IOException {
        return input.read();
      }
    };
    expect(request.getInputStream()).andStubReturn(in);
    expect(request.getHeaderNames()).andStubReturn(
        Iterators.asEnumeration(ImmutableList.of("Host").iterator()));
    expect(request.getHeaders("Host")).andStubReturn(
        Iterators.asEnumeration(ImmutableList.of("jstd:80").iterator()));
    expect(request.getQueryString()).andStubReturn("?id=123");
    Capture<HttpMethodBase> methodCapture = new Capture<HttpMethodBase>();
    expect(client.executeMethod(EasyMock.capture(methodCapture))).andStubReturn(200);
    /* expect */ response.setStatus(200);
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    ServletOutputStream out = new ServletOutputStream() {
      @Override
      public void write(int b) throws IOException {
        output.write(b);
      }
    };
    expect(response.getOutputStream()).andStubReturn(out);
    control.replay();
    gateway.init(config);
    gateway.service(request, response);
    ByteArrayOutputStream requestBody = new ByteArrayOutputStream();
    ((EntityEnclosingMethod) methodCapture.getValue()).getRequestEntity().writeRequest(requestBody);
    assertEquals("ASDF", requestBody.toString());
    assertEquals("", output.toString());
  }
}
