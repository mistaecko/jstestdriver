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

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class FileSource {

  private String fileSrc;
  private String basePath;
  private long timestamp;
  private long length;

  public FileSource() {
  }

  public FileSource(String fileSrc, String basePath, long timestamp, long length) {
    this.fileSrc = fileSrc;
    this.basePath = basePath;
    this.timestamp = timestamp;
    this.length = length;
  }

  public String getFileSrc() {
    return fileSrc;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public String getBasePath() {
    return basePath;
  }
  
  /**
   * @return the length
   */
  public long getLength() {
    return length;
  }

  /**
   * @param length the length to set
   */
  public void setLength(long length) {
    this.length = length;
  }

  /**
   * @param timestamp the timestamp to set
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public void setFileSource(String fileSrc) {
    this.fileSrc = fileSrc;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }
  
  @Override
  public String toString() {
    return String.format("%s(%s, %s, %s)", getClass().getSimpleName(), fileSrc, basePath, timestamp);
  }
  
  public FileInfo toFileInfo(String contents) {
    return new FileInfo(this.getBasePath(), this.getTimestamp(),
      this.getLength(), false, false, contents, this.getFileSrc());
  }
}
