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
      FileInfo oldFile = files.getFile(file.getFilePath());
      if (oldFile == null) {
        files.addFile(file);
        logger.debug("not replacing {} with {}", oldFile, file);
      } else if (oldFile.shouldReplaceWith(file)) {
        logger.debug("replacing {} with {}", oldFile, file);
        files.addFile(file);
      } else if (oldFile.isLoaded()){
        logger.debug("not replacing {} (loaded) with {}", oldFile, file);
        // the old file the same as the new, except the old is loaded.
        replace.add(oldFile);
      } else if (file.isLoaded()) {
        files.addFile(file);
      } else {
        logger.debug("files are equal {}, {}", oldFile, file);
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
      for (Entry<String, JstdTestCase> entry : cases.entrySet()) {
        entry.setValue(entry.getValue().applyDelta(delta));
      }
    }
  }
}
