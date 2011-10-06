package com.google.jstestdriver.servlet.fileset;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.browser.BrowserFileSet;
import com.google.jstestdriver.model.JstdTestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author corbinrsmith@gmail.com (Cory Smith)
 */
public class BrowserFileCheck implements FileSetRequestHandler<BrowserFileSet> {

  private static final Logger logger = LoggerFactory.getLogger(BrowserFileCheck.class);

  public static final String ACTION = "browserFileCheck";

  private final Gson gson;

  @Inject
  public BrowserFileCheck(Gson gson) {
    this.gson = gson;
  }

  @Override
  public BrowserFileSet handle(SlaveBrowser browser, String data) {
    JstdTestCase testCase = gson.fromJson(data, JstdTestCase.class);
    if (browser == null) {
      logger.debug("no browser, returning empty set.");
      return new BrowserFileSet(Collections.<FileInfo>emptyList(),
          Collections.<FileInfo>emptyList(), false);
    }
    final List<FileInfo> filesToUpdate = Lists.newLinkedList();
    final List<FileInfo> extraFiles = Lists.newLinkedList();
    boolean reset = false;
    logger.debug("Determing files to update {}, {}", testCase.toFileSet(), browser.getFileSet());
    for (FileInfo newFile : testCase.getServable()) {
      if (browser.getFileSet().contains(newFile)) {
        for (FileInfo oldFile : browser.getFileSet()) {
          if (oldFile.shouldReplaceWith(newFile)) {
            filesToUpdate.add(newFile);
          }
        }
      } else {
        filesToUpdate.add(newFile);
      }
    }
    extraFiles.addAll(browser.getFileSet());
    extraFiles.removeAll(testCase.toFileSet());

    if (!(filesToUpdate.isEmpty() && extraFiles.isEmpty())
        && (browser.getBrowserInfo().getName().contains("Safari")
            || browser.getBrowserInfo().getName().contains("Opera")
            || browser.getBrowserInfo().getName().contains("Konqueror"))) {
      // reload all files if Safari, Opera, or Konqueror, because they don't
      // overwrite properly.
      // TODO(corysmith): Replace this with polymorphic browser classes.
      logger.info("Resetting browser fileset to ensure proper overwriting. {} {}", filesToUpdate.isEmpty(), extraFiles);
      filesToUpdate.addAll(testCase.toFileSet());
      // TODO(corysmith): Change the browser to handle it's own resets.
      browser.resetFileSet();
      reset = true;
    }

    return new BrowserFileSet(filesToUpdate, extraFiles, reset);
  }
  

  @Override
  public boolean canHandle(String action) {
    return ACTION.equalsIgnoreCase(action);
  }
}