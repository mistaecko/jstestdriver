// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.servlet.fileset;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.model.JstdTestCaseDelta;
import com.google.jstestdriver.server.JstdTestCaseStore;

import java.util.Collection;

/**
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class DeltaUpload implements FileSetRequestHandler<String> {
  
  public static final String ACTION = "deltaUpdate";
  private final JstdTestCaseStore store;
  private final Gson gson;
  
  @Inject
  public DeltaUpload(JstdTestCaseStore store, Gson gson) {
    this.store = store;
    this.gson = gson;
  }
  
  @Override
  public String handle(SlaveBrowser browser, String data) {
    Collection<JstdTestCaseDelta> deltas = deserialize(data);
    for (JstdTestCaseDelta delta : deltas) {
      store.applyDelta(delta);
    }
    return "{\"ok\":1}";
  }
  

  private Collection<JstdTestCaseDelta> deserialize(String data) {
    return gson.fromJson(data, new TypeToken<Collection<JstdTestCaseDelta>>() {}.getType());
  }

  @Override
  public boolean canHandle(String action) {
    return ACTION.equals(action);
  }

}
