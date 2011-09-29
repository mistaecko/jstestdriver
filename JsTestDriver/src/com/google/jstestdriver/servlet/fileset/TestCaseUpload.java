package com.google.jstestdriver.servlet.fileset;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.model.JstdTestCaseDelta;
import com.google.jstestdriver.server.JstdTestCaseStore;

import java.util.Collection;
import java.util.List;

/**
 * Handles uploads to the server file cache.
 * @author corbinrsmith@gmail.com (Cory Smith)
 */
public class TestCaseUpload implements FileSetRequestHandler<Collection<JstdTestCaseDelta>> {
  public static final String ACTION = "serverFileUpload";
  private final JstdTestCaseStore store;
  private final Gson gson;

  @Inject
  public TestCaseUpload(JstdTestCaseStore store, Gson gson) {
    this.store = store;
    this.gson = gson;
    
  }

  @Override
  public Collection<JstdTestCaseDelta> handle(SlaveBrowser browser, String data) {
    Collection<JstdTestCase> testCases = deserialize(data);
    List<JstdTestCaseDelta> deltas = Lists.newArrayList(); 
    for (JstdTestCase testCase : testCases) {
      deltas.add(store.addCase(testCase));
    }
    return deltas;
  }
  

  @Override
  public boolean canHandle(String action) {
    return ACTION.equalsIgnoreCase(action);
  }

  private Collection<JstdTestCase> deserialize(String data) {
    return gson.fromJson(data, new TypeToken<Collection<JstdTestCase>>() {}.getType());
  }
}