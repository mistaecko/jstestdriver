// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FilesCache;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.model.JstdTestCaseDelta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A store for JstdTestCases.
 * @author corysmith@google.com (Cory Smith)
 *
 */
@Singleton
public class JstdTestCaseStore {
  private static final Logger logger = LoggerFactory.getLogger(JstdTestCaseStore.class);
  private final ConcurrentMap<String, JstdTestCase> cases =
      new ConcurrentHashMap<String, JstdTestCase>();
  private final FilesCache files = new FilesCache(Maps.<String, FileInfo>newHashMap());



  /**
   * Adds a testcase to the store, returning a delta of files that need to be
   * loaded.
   */
  public JstdTestCaseDelta addCase(JstdTestCase testCase) {
    JstdTestCase updatedTestCase;
    synchronized (files) {
      updatedTestCase = testCase.applyDelta(new JstdTestCaseDelta(
          updateCache(testCase.getDependencies()),
          updateCache(testCase.getTests()),
          updateCache(testCase.getPlugins())));
    }
    logger.info("adding TestCase {} to {}", testCase.getId(), this);
    cases.put(testCase.getId(), updatedTestCase);
    return updatedTestCase.createUnloadedDelta();
  }

  /**
   * Updates the files cache with files and returns ones that are already to
   * update the testcase with.
   */
  private List<FileInfo> updateCache(List<FileInfo> newFiles) {
    List<FileInfo> replace = Lists.newArrayList();
    for (FileInfo file : newFiles) {
      FileInfo oldFile = files.getFile(file.getDisplayPath());
      if (oldFile == null) {
        files.addFile(file);
        logger.debug("adding {}", file.getDisplayPath());
      } else if (oldFile.shouldReplaceWith(file)) {
        logger.debug("replacing {}", oldFile.getDisplayPath());
        files.addFile(file);
      } else if (file.isLoaded()) {
        logger.debug("updating {} (loaded)", file.getDisplayPath());
        files.addFile(file);
      } else if (oldFile.isLoaded() && !file.isLoaded()){
        logger.debug("not replacing {}", file.getDisplayPath());
        // the old file the same as the new, except the old is loaded.
        replace.add(oldFile);
      } else {
        logger.debug("files are equal {}", file.getDisplayPath());
      }
    }
    return replace;
  }

  /**
   * Returns a collection of all the test cases.
   */
  public Collection<JstdTestCase> getCases() {
    return cases.values();
  }

  /**
   * Retrieve a test case from the store.
   */
  public JstdTestCase getCase(String testCaseId) {
    if (testCaseId == null) {
      return null;
    }
    return cases.get(testCaseId);
  }

  /**
   * Returns the contents of a given filename.
   */
  // TODO(corysmith): Workaround until the semantics of loading files are worked
  // out. Ideally, there should be some way of associating a file with a slaved browser.
  public String getFileContent(String path) {
    return files.getFileContent(path);
  }

  /**
   * Applies a JstdTestCaseDelta to the file cache and all test cases.
   */
  public void applyDelta(JstdTestCaseDelta delta) {
    synchronized (files) {
      updateCache(delta.getDependencies());
      updateCache(delta.getTests());
      updateCache(delta.getPlugins());
    }
    synchronized (cases) {
      Map<String, JstdTestCase> applied = Maps.newHashMap();
      for (Entry<String, JstdTestCase> entry : cases.entrySet()) {
        applied.put(entry.getKey(), entry.getValue().applyDelta(delta));
      }
      cases.putAll(applied);
    }
  }
}
