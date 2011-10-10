package com.google.jstestdriver.server.handlers;

import com.google.common.collect.Maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author corysmith@google.com (Cory Smith)
 *
 */
final class FakeHttpServletRequest implements HttpServletRequest {
  private Map<String, String> parameters = Maps.newHashMap();

  @Override
  public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
    // TODO(corysmith): Auto-generated method stub
    
  }

  @Override
  public void setAttribute(String arg0, Object arg1) {
    // TODO(corysmith): Auto-generated method stub
    
  }

  @Override
  public void removeAttribute(String arg0) {
    // TODO(corysmith): Auto-generated method stub
    
  }

  @Override
  public boolean isSecure() {
    // TODO(corysmith): Auto-generated method stub
    return false;
  }

  @Override
  public int getServerPort() {
    // TODO(corysmith): Auto-generated method stub
    return 0;
  }

  @Override
  public String getServerName() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getScheme() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String arg0) {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public int getRemotePort() {
    // TODO(corysmith): Auto-generated method stub
    return 0;
  }

  @Override
  public String getRemoteHost() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getRemoteAddr() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getRealPath(String arg0) {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public BufferedReader getReader() throws IOException {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getProtocol() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String[] getParameterValues(String arg0) {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public Enumeration getParameterNames() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public Map getParameterMap() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getParameter(String key) {
    return parameters.get(key);
  }

  @Override
  public Enumeration getLocales() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public Locale getLocale() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public int getLocalPort() {
    // TODO(corysmith): Auto-generated method stub
    return 0;
  }

  @Override
  public String getLocalName() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getLocalAddr() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getContentType() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public int getContentLength() {
    // TODO(corysmith): Auto-generated method stub
    return 0;
  }

  @Override
  public String getCharacterEncoding() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public Enumeration getAttributeNames() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public Object getAttribute(String arg0) {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public boolean isUserInRole(String arg0) {
    // TODO(corysmith): Auto-generated method stub
    return false;
  }

  @Override
  public boolean isRequestedSessionIdValid() {
    // TODO(corysmith): Auto-generated method stub
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromUrl() {
    // TODO(corysmith): Auto-generated method stub
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {
    // TODO(corysmith): Auto-generated method stub
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {
    // TODO(corysmith): Auto-generated method stub
    return false;
  }

  @Override
  public Principal getUserPrincipal() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public HttpSession getSession(boolean arg0) {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public HttpSession getSession() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getServletPath() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getRequestedSessionId() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public StringBuffer getRequestURL() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getRequestURI() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getRemoteUser() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getQueryString() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getPathTranslated() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getPathInfo() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getMethod() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public int getIntHeader(String arg0) {
    // TODO(corysmith): Auto-generated method stub
    return 0;
  }

  @Override
  public Enumeration getHeaders(String arg0) {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public Enumeration getHeaderNames() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getHeader(String arg0) {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public long getDateHeader(String arg0) {
    // TODO(corysmith): Auto-generated method stub
    return 0;
  }

  @Override
  public Cookie[] getCookies() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getContextPath() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  @Override
  public String getAuthType() {
    // TODO(corysmith): Auto-generated method stub
    return null;
  }

  /**
   * @param string
   * @param string2
   */
  public void setParameter(String key, String value) {
    parameters.put(key, value);
  }
}