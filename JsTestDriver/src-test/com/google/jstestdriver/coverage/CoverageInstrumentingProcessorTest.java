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
package com.google.jstestdriver.coverage;

import java.util.Collections;

import org.joda.time.Instant;

import junit.framework.TestCase;

import com.google.common.collect.Sets;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.Time;

/**
 * @author corbinrsmith@gmail.com (Cory Smith)
 */
public class CoverageInstrumentingProcessorTest extends TestCase {

  public void testInstrument() throws Exception {
    FileInfo fileInfo = new FileInfo("foo.js", 0, -1, true, false, "var a = 1;", "foo.js");
    String expected = "decorated";
    Code code = new Code(fileInfo.getFilePath(), fileInfo.getData());
    CoverageAccumulator accumulator = new CoverageAccumulator();
    final Instant instant = new Instant(System.currentTimeMillis());
    FileInfo decorated =
        new CoverageInstrumentingProcessor(new DecoratorStub(expected, code),
            Collections.<String>emptySet(), accumulator, new Time() {
              public Instant now() {
                return instant;
              }
            }).process(fileInfo);
    assertEquals(expected, decorated.getData());
    assertEquals(fileInfo.getFilePath(), decorated.getFilePath());
    assertEquals(instant.getMillis(), decorated.getTimestamp());
    assertEquals(fileInfo.isServeOnly(), decorated.isServeOnly());
  }

  public void testSkipInstrument() throws Exception {
    FileInfo lcov = new FileInfo("LCOV.js", 0, -1, true, false, "var a = 1;", "LCOV.js");
    FileInfo serveOnly = new FileInfo("someData.dat", 0, -1, true, true, "scally{wag}", "someData.dat");
    FileInfo excluded = new FileInfo("excluded.dat", 0, -1, true, false, "not{me}", "excluded.dat");
    FileInfo remote = new FileInfo("https://foobar", 0, -1, true, false, null, "https://foobar");
    FileInfo empty = new FileInfo("foobar.js", 0, -1, true, false, "\n", "foobar.js");
    CoverageInstrumentingProcessor processor =
        new CoverageInstrumentingProcessor(null,
            Sets.<String>newHashSet(excluded.getFilePath()),
            null, null);
    assertSame(lcov, processor.process(lcov));
    assertSame(serveOnly, processor.process(serveOnly));
    assertSame(remote, processor.process(remote));
    assertSame(excluded, processor.process(excluded));
    assertSame(empty, processor.process(empty));
  }

  static class DecoratorStub extends CodeInstrumentor {
    private final String decorated;
    private final Code expectedCode;
    public DecoratorStub(String decorated, Code expectedCode) {
      super(null);
      this.decorated = decorated;
      this.expectedCode = expectedCode;
    }

    @Override
    public InstrumentedCode instrument(Code code) {
      assertEquals(expectedCode, code);
      return new InstrumentedCode(-1,
          "", Collections.<Integer>emptyList(), decorated);
    }
  }
}
