/**
 * 
 */
package com.google.jstestdriver.server.handlers.pages;

import com.google.jstestdriver.util.HtmlWriter;

import java.io.IOException;

/**
 * Common interface for runner pages.
 * 
 * @author corbinrsmith@gmail.com (Cory Smith)
 */
public interface Page {
  void render(HtmlWriter writer, SlavePageRequest request) throws IOException;
}