// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.util;

/**
 * Thrown when the number of retries is exhausted.
 * @author corbinrsmith@gmail.com (Cory Smith)
 */
public class RetryException extends RuntimeException {

  private final int tries;

  public RetryException(int tries, String message, Throwable cause) {
    super(message, cause);
    this.tries = tries;
  }

  @Override
  public String toString() {
    return "Tried " + tries + "times: \n" + super.toString();
  }
}
