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

import junit.framework.TestCase;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class UserAgentParserTest extends TestCase {

  private static final String CHROME_WINDOWS = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) " +
  		"AppleWebKit/530.5 (KHTML, like Gecko) Chrome/2.0.172.31 Safari/530.5";

  private static final String FIREFOX_WINDOWS = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; " +
  		"rv:1.9.0.10) Gecko/2009042316 Firefox/3.0.10 (.NET CLR 3.5.30729)";

  private static final String SAFARI_WINDOWS = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) " +
  		"AppleWebKit/525.28.3 (KHTML, like Gecko) Version/3.2.3 Safari/525.29";

  private static final String IE_WINDOWS = "Mozilla/4.0 " +
  		"(compatible; MSIE 7.0; Windows NT 5.1; InfoPath.2; .NET CLR 2.0.50727; " +
  		".NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)";

  private static final String OPERA_OLD_WINDOWS = "Opera/9.64(Windows NT 5.1; U; en) Presto/2.1.1";

  private static final String OPERA_NEW_WINDOWS = "Opera/9.80 (Windows NT 6.0; U; en) " +
      "Presto/2.8.99 Version/11.10";

  private static final String FIREFOX_LINUX = "Mozilla/5.0 " +
  		"(X11; U; Linux x86_64; en-US; rv:1.9.0.10) Gecko/2009042513 Ubuntu/8.04 (hardy) " +
  		"Firefox/3.0.10";

  private static final String OPERA_OLD_LINUX = "Opera/9.64 (X11; Linux x86_64; U; pl) Presto/2.1.1";

  private static final String OPERA_NEW_LINUX = "Opera/9.80 (X11; Linux x86_64; U; Opera Next; " +
      "en) Presto/2.8.131 Version/11.50";

  private static final String FIREFOX_MACOS = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; " +
  		"en-US; rv:1.9.0.10) Gecko/2009042315 Firefox/3.0.10";

  private static final String SAFARI_MACOS = "Mozilla/5.0 " +
  		"(Macintosh; U; Intel Mac OS X 10_5_7; en-us) AppleWebKit/528.16 (KHTML, like Gecko) " +
  		"Version/4.0 Safari/528.16";

  private static final String OPERA_OLD_MACOS = "Opera/9.61 (Macintosh; Intel Mac OS X; U; de) " +
      "Presto/2.1.1";

  private static final String OPERA_NEW_MACOS = "Opera/9.80 (Macintosh; Intel Mac OS X; U; nl) " +
      "Presto/2.6.30 Version/10.61";

  private static final String SAFARI_IPHONE = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_1_2 like " +
      "Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) " +
      "Version/4.0 Mobile/7D11 Safari/528.16";

  private static final String SAFARI_ANDROID = "Mozilla/5.0 (Linux; U; Android 2.2.1; en-us; " +
      "Nexus One Build/FRG83) AppleWebKit/533.1 (KHTML, like Gecko) " +
      "Version/4.0 Mobile Safari/533.1";

  public void testGetBrowserName() throws Exception {
    UserAgentParser parser = null;

    parser = new UserAgentParser();
    parser.parse(CHROME_WINDOWS);
    assertEquals("Chrome", parser.getName());
    assertEquals("Windows", parser.getOs());

    parser = new UserAgentParser();
    parser.parse(FIREFOX_WINDOWS);
    assertEquals("Firefox", parser.getName());
    assertEquals("Windows", parser.getOs());

    parser = new UserAgentParser();
    parser.parse(FIREFOX_LINUX);
    assertEquals("Firefox", parser.getName());
    assertEquals("Linux", parser.getOs());

    parser = new UserAgentParser();
    parser.parse(FIREFOX_MACOS);
    assertEquals("Firefox", parser.getName());
    assertEquals("Mac OS", parser.getOs());

    parser = new UserAgentParser();
    parser.parse(SAFARI_WINDOWS);
    assertEquals("Safari", parser.getName());
    assertEquals("Windows", parser.getOs());

    parser = new UserAgentParser();
    parser.parse(SAFARI_MACOS);
    assertEquals("Safari", parser.getName());
    assertEquals("Mac OS", parser.getOs());

    parser = new UserAgentParser();
    parser.parse(IE_WINDOWS);
    assertEquals("Microsoft Internet Explorer", parser.getName());
    assertEquals("Windows", parser.getOs());

    parser = new UserAgentParser();
    parser.parse(OPERA_OLD_WINDOWS);
    assertEquals("Opera", parser.getName());
    assertEquals("9.64", parser.getVersion());
    assertEquals("Windows", parser.getOs());

    parser = new UserAgentParser();
    parser.parse(OPERA_NEW_WINDOWS);
    assertEquals("Opera", parser.getName());
    assertEquals("11.10", parser.getVersion());
    assertEquals("Windows", parser.getOs());

    parser = new UserAgentParser();
    parser.parse(OPERA_OLD_LINUX);
    assertEquals("Opera", parser.getName());
    assertEquals("9.64", parser.getVersion());
    assertEquals("Linux", parser.getOs());

    parser = new UserAgentParser();
    parser.parse(OPERA_NEW_LINUX);
    assertEquals("Opera", parser.getName());
    assertEquals("11.50", parser.getVersion());
    assertEquals("Linux", parser.getOs());

    parser = new UserAgentParser();
    parser.parse(OPERA_OLD_MACOS);
    assertEquals("Opera", parser.getName());
    assertEquals("9.61", parser.getVersion());
    assertEquals("Mac OS", parser.getOs());

    parser = new UserAgentParser();
    parser.parse(OPERA_NEW_MACOS);
    assertEquals("Opera", parser.getName());
    assertEquals("10.61", parser.getVersion());
    assertEquals("Mac OS", parser.getOs());

    parser = new UserAgentParser();
    parser.parse(SAFARI_IPHONE);
    assertEquals("Safari", parser.getName());
    assertEquals("iPhone OS", parser.getOs());

    parser = new UserAgentParser();
    parser.parse(SAFARI_ANDROID);
    assertEquals("Safari", parser.getName());
    assertEquals("Android", parser.getOs());

    parser = new UserAgentParser();
    parser.parse("Some weird unrecognized user-agent");
    assertEquals("Some weird unrecognized user-agent", parser.getName());
  }
}
