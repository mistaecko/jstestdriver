// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.hooks;

import com.google.jstestdriver.BrowserInfo;

/**
 * Defines the events that happen during the JsTestdriverServer lifecycle.
 * @author corysmith@google.com (Cory Smith)
 *
 */
public interface ServerListener {
  /**
   * Called when the server starts up.
   */
  public void serverStarted();

  /**
   * Called when the server stops.
   */
  public void serverStopped();

  /**
   * Called when a new browser is captured.
   * @param info The information about the new browser.
   */
  public void browserCaptured(BrowserInfo info);

  /**
   * Called when a browser "panics" or become inaccessible to the runner.
   * @param info The information about the lost browser.
   */
  public void browserPanicked(BrowserInfo info);
}
