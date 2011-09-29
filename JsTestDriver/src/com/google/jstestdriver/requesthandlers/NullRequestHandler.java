// Copyright 2011 Google Inc. All Rights Reserved.
package com.google.jstestdriver.requesthandlers;

import java.io.IOException;

/**
 * A {@link RequestHandler} with no behavior.
 * @author rdionne@google.com (Robert Dionne)
 */
public class NullRequestHandler implements RequestHandler {

  public void handleIt() throws IOException {}
}
