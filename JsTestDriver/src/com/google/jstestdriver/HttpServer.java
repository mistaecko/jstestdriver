/*
 * Copyright 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver;

import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.jstestdriver.util.StopWatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class HttpServer implements Server {
  private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
  private final StopWatch stopWatch;
  
  @Inject
  public HttpServer(StopWatch stopWatch) {
    this.stopWatch = stopWatch;
  }

  @Override
  public String fetch(String url) {
    stopWatch.start("fetch %s", url);
    HttpURLConnection connection = null;
    logger.trace("Fetching {}", url);

    try {
      connection = (HttpURLConnection) new URL(url).openConnection();
      connection.connect();
      String response = toString(connection.getInputStream());
      logger.trace("Fetch response {}", response);
      return response;
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
      stopWatch.stop("fetch %s", url);
    }
  }

  public void ping(String url) {
    HttpURLConnection connection = null;
    logger.trace("Pinging {}", url);
    
    try {
      connection = (HttpURLConnection) new URL(url).openConnection();
      
      connection.connect();
      connection.getInputStream();
      logger.trace("Pinged {}", url);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }
  
  

  private String toString(InputStream inputStream) throws IOException {
    StringBuilder sb = new StringBuilder();
    int ch;

    while ((ch = inputStream.read()) != -1) {
      sb.append((char) ch);
    }
    inputStream.close();
    return sb.toString();
  }

  @Override
  public String post(String url, Map<String, String> params) {
    stopWatch.start("post %s", url);
    HttpURLConnection connection = null;

    try {
      logger.trace("Post url:{} \nParams:\n{} \n", url, params);
      String paramsString = convertParamsToString(params);

      connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      connection.setRequestProperty("Content-Length", Integer
          .toString(paramsString.getBytes().length));
      OutputStreamWriter oWriter = new OutputStreamWriter(connection.getOutputStream());

      oWriter.write(paramsString);
      oWriter.close();
      connection.connect();
      String response = toString(connection.getInputStream());
      logger.trace("Post response:\n{}", response);
      return response;
    } catch (IOException e) {
      throw new RuntimeException("Connection error on : " +connection.toString(), e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
      stopWatch.stop("post %s", url);
    }
  }

  @Override
  public String postJson(String url, JsonElement json) {
    stopWatch.start("postJson %s", url);
    HttpURLConnection connection = null;
    try {
      logger.trace("Post url:{}\nJSON:\n{}\n", url, json);
      String jsonString = json.toString();
      connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.setRequestProperty("Content-Type", "application/jsonrequest");
      connection.setRequestProperty("Content-Length", Integer.toString(
          jsonString.getBytes().length));
      OutputStreamWriter oWriter = new OutputStreamWriter(connection.getOutputStream());
      oWriter.write(jsonString);
      oWriter.close();
      connection.connect();
      String response = toString(connection.getInputStream());
      logger.trace("Post response:\n{}\n", response);
      return response;
    } catch (IOException e) {
      throw new RuntimeException("Connection error on: " + connection, e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
      stopWatch.stop("postJson %s", url);
    }
  }

  public String convertParamsToString(Map<String, String> params)
      throws UnsupportedEncodingException {
    StringBuilder sb = new StringBuilder();
    Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();

    // TODO(corysmith): WTF? Figure out what's going on....
    
    if (iterator.hasNext()) {
      Map.Entry<String, String> entry = iterator.next();

      sb.append(String.format("%s=%s", URLEncoder.encode(entry.getKey(), "UTF-8"),
          URLEncoder.encode(entry.getValue(), "UTF-8")));
      while (iterator.hasNext()) {
        entry = iterator.next();
        sb.append(String.format("&%s=%s", URLEncoder.encode(entry.getKey(), "UTF-8"),
            URLEncoder.encode(entry.getValue(), "UTF-8")));
      }
    }
    return sb.toString();
  }

  @Override
  public String startSession(String baseUrl, String id) {
    return fetch(baseUrl + "/fileSet?id=" + id + "&session=start");
  }

  @Override
  public void stopSession(String baseUrl, String id, String sessionId) {
    fetch(baseUrl + "/fileSet?id=" + id + "&session=stop" + "&sessionId=" + sessionId);
  }
}
