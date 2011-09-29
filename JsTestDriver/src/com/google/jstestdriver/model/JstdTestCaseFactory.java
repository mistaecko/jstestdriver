// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.jstestdriver.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.hooks.JstdTestCaseProcessor;
import com.google.jstestdriver.hooks.ResourceDependencyResolver;
import com.google.jstestdriver.util.StopWatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A factory for the creation of test cases.
 * @author corysmith@google.com (Cory Smith)
 */
public class JstdTestCaseFactory {

  private static final Logger logger =
      LoggerFactory.getLogger(JstdTestCaseFactory.class);
  private final Set<JstdTestCaseProcessor> processors;
  private final Set<ResourceDependencyResolver> resolvers;
  private final StopWatch stopWatch;

  @Inject
  public JstdTestCaseFactory(Set<JstdTestCaseProcessor> processors,
                             Set<ResourceDependencyResolver> resolvers,
                             StopWatch stopWatch) {
    this.processors = processors;
    this.resolvers = resolvers;
    this.stopWatch = stopWatch;
  }

  public List<JstdTestCase> createCases(
      List<FileInfo> plugins,
      List<FileInfo> deps,
      List<FileInfo> tests) {
    stopWatch.start("Create TestCases");
    try {
      final List<JstdTestCase> testCases = Lists.newArrayList();
      if (!(deps.isEmpty() && tests.isEmpty())) {
        logger.debug("creating default test case");
        testCases.add(new JstdTestCase(deps, tests, plugins, "default"));
      }
      return processTestCases(resolveDependencies(testCases));
    } finally {
      stopWatch.stop("Create TestCases");
    }
  }

  private List<JstdTestCase> resolveDependencies(List<JstdTestCase> testCases) {
    stopWatch.start("resolveDependencies");
    try {
      final List<JstdTestCase> resolved = Lists.newArrayListWithExpectedSize(testCases.size());
      for (JstdTestCase jstdTestCase : testCases) {
        for (ResourceDependencyResolver resolver : resolvers) {
          stopWatch.start(resolver.toString());
          jstdTestCase = resolver.resolve(jstdTestCase);
          stopWatch.stop(resolver.toString());
        }
        resolved.add(jstdTestCase);
      }
      return resolved;
    } finally {
      stopWatch.stop("resolveDependencies");
    }
  }

  private List<JstdTestCase> processTestCases(List<JstdTestCase> testCases) {
    stopWatch.start("resolveDependencies");
    try {
      for (JstdTestCaseProcessor processor : processors) {
        stopWatch.start(processor.toString());
        testCases = processor.process(testCases.iterator());
        stopWatch.stop(processor.toString());
      }
      return testCases;
    } finally {
      stopWatch.stop("resolveDependencies");
    }
  }

  // TODO(corysmith): Remove when RunData no longer allows access to the FileSet.
  public List<JstdTestCase> updateCases(Set<FileInfo> fileSet, List<JstdTestCase> testCases) {
    Set<FileInfo> tests = Sets.newLinkedHashSet();
    for (JstdTestCase testCase : testCases) {
      tests.addAll(testCase.getTests());
    }
    fileSet.removeAll(tests);
    return createCases(Collections.<FileInfo>emptyList(), Lists.newArrayList(fileSet), Lists.newArrayList(tests));
  }
}
