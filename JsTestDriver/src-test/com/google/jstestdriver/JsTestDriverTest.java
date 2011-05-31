// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver;

import com.google.jstestdriver.config.DefaultConfiguration;
import com.google.jstestdriver.embedded.JsTestDriverBuilder;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.runner.RunnerMode;

import junit.framework.TestCase;

import java.io.File;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class JsTestDriverTest extends TestCase {

  private final class TestListener implements ServerListener {
    boolean serverStopped;
    boolean serverStarted;
    private final CountDownLatch startLatch;
    private final CountDownLatch stopLatch;

    /**
     * @param startLatch
     * @param stopLatch
     */
    public TestListener(CountDownLatch startLatch, CountDownLatch stopLatch) {
      this.startLatch = startLatch;
      this.stopLatch = stopLatch;
    }

    @Override
    public void serverStopped() {
      serverStopped = true;
      stopLatch.countDown();
    }

    @Override
    public void serverStarted() {
      serverStarted = true;
      startLatch.countDown();
    }

    @Override
    public void browserPanicked(BrowserInfo info) {
      
    }

    @Override
    public void browserCaptured(BrowserInfo info) {
      
    }
  }

  /**
   * @author corysmith@google.com (Cory Smith)
   *
   */
  private final class TestConfiguration extends DefaultConfiguration {
    public TestConfiguration(File basePath) {
      super(basePath);
    }
  }

  public void testServerStartAndStop() throws Exception {
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch stopLatch = new CountDownLatch(1);
    TestListener testServerListener = new TestListener(startLatch, stopLatch);
    JsTestDriver jstd = new JsTestDriverBuilder()
        .setDefaultConfiguration(new TestConfiguration(new File(".")))
        .setRunnerMode(RunnerMode.DEBUG)
        .addServerListener(testServerListener)
        .setPort(8080)
        .build();
    jstd.startServer();
    startLatch.await(10, TimeUnit.SECONDS);
    assertTrue(testServerListener.serverStarted);
    jstd.stopServer();
    stopLatch.await(10, TimeUnit.SECONDS);
    assertTrue(testServerListener.serverStopped);
  }
}
