// Copyright 2011 Google Inc. All Rights Reserved.
package com.google.jstestdriver.server.gateway;

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
  private GatewayRequestHandler gateway;
  private HttpServletRequest request;
  private HttpServletResponse response;

  @Override
  protected void setUp() throws Exception {
    control = EasyMock.createControl();
    client = control.createMock(HttpClient.class);
    request = control.createMock(HttpServletRequest.class);
    response = control.createMock(HttpServletResponse.class);
    gateway = new GatewayRequestHandler(client, request, response, "http://hostname:80", null);
  }

  @Override
  protected void tearDown() throws Exception {
    control.verify();
  }

  public void testService_GET() throws Exception {
    expect(request.getMethod()).andStubReturn("GET");
    expect(request.getRequestURI()).andStubReturn("/relativeUri");
    expect(request.getHeaderNames()).andStubReturn(
        Iterators.asEnumeration(ImmutableList.of("Host").iterator()));
    expect(request.getHeaders("Host")).andStubReturn(
        Iterators.asEnumeration(ImmutableList.of("jstd:80").iterator()));
    expect(request.getQueryString()).andStubReturn("id=123");
    // TODO(rdionne): Feed fake response values into the captured HttpMethod and assert they are
    // properly converted to equivalent HttpServletResponse fields.
    Capture<HttpMethodBase> methodCapture = new Capture<HttpMethodBase>();
    expect(client.executeMethod(EasyMock.capture(methodCapture))).andStubReturn(200);
    /* expect */ response.setStatus(200);
    expect(request.getHeaders("Pragma")).andStubReturn(
        Iterators.asEnumeration(Iterators.emptyIterator()));
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    ServletOutputStream out = new ServletOutputStream() {
      @Override
      public void write(int b) throws IOException {
        output.write(b);
      }
    };
    expect(response.getOutputStream()).andStubReturn(out);
    control.replay();
    gateway.handleIt();
    assertEquals("GET", methodCapture.getValue().getName());
    assertEquals("http://hostname/relativeUri?id=123", methodCapture.getValue().getURI().toString());
    assertEquals("hostname:80", methodCapture.getValue().getRequestHeader("Host").getValue());
    assertEquals("id=123", methodCapture.getValue().getQueryString());
    assertEquals("", output.toString());
  }

  public void testService_POST() throws Exception {
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
    expect(request.getQueryString()).andStubReturn("id=123");
    // TODO(rdionne): Feed fake response values into the captured HttpMethod and assert they are
    // properly converted to equivalent HttpServletResponse fields.
    Capture<HttpMethodBase> methodCapture = new Capture<HttpMethodBase>();
    expect(client.executeMethod(EasyMock.capture(methodCapture))).andStubReturn(200);
    /* expect */ response.setStatus(200);
    expect(request.getHeaders("Pragma")).andStubReturn(
        Iterators.asEnumeration(Iterators.emptyIterator()));
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    ServletOutputStream out = new ServletOutputStream() {
      @Override
      public void write(int b) throws IOException {
        output.write(b);
      }
    };
    expect(response.getOutputStream()).andStubReturn(out);
    control.replay();
    gateway.handleIt();
    ByteArrayOutputStream requestBody = new ByteArrayOutputStream();
    ((EntityEnclosingMethod) methodCapture.getValue()).getRequestEntity().writeRequest(requestBody);
    assertEquals("POST", methodCapture.getValue().getName());
    assertEquals("http://hostname/relativeUri?id=123", methodCapture.getValue().getURI().toString());
    assertEquals("hostname:80", methodCapture.getValue().getRequestHeader("Host").getValue());
    assertEquals("id=123", methodCapture.getValue().getQueryString());
    assertEquals("ASDF", requestBody.toString());
    assertEquals("", output.toString());
  }
}
