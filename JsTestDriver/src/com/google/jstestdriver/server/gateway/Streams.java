// Copyright 2011 Google, Inc. All Rights Reserved.
package com.google.jstestdriver.server.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utilities for copying streams, used by {@link GatewayRequestHandler}.
 * @author rdionne@google.com (Robert Dionne)
 */
final class Streams {

  private static final int BUFFER_SIZE = 0x1000;

  /**
   * Borrowed from
   * http://code.google.com/p/guava-libraries/source/browse/trunk/guava/src/com/google/common/io/ByteStreams.java
   * @param from
   * @param to
   * @return
   * @throws IOException
   */
  public static long copy(InputStream from, OutputStream to) throws IOException {
    if (from == null) {
      return 0L;
    }
    byte[] buffer = new byte[BUFFER_SIZE];
    long total = 0L;
    while (true) {
      int read = from.read(buffer);
      if (read == -1) {
        break;
      }
      to.write(buffer, 0, read);
      total += read;
    }
    return total;
  }

  private Streams() {}
}
