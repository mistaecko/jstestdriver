// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.jstestdriver.html;

import com.google.jstestdriver.token.Node;
import com.google.jstestdriver.token.Token;

import java.io.Writer;
import java.util.List;

/**
 * Contains the html of the htmldoc.
 * 
 * @author corbinrsmith@gmail.com (Cory Smith)
 */
public class HtmlNode implements Node {

  private final List<Token> htmlTokens;
  public HtmlNode(List<Token> htmlTokens) {
    this.htmlTokens = htmlTokens;
  }

  public void write(Writer writer) {
    for (Token token : htmlTokens) {
      token.write(writer);
    }
  }
}
