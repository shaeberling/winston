/*
 * Copyright 2016 The Winston Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.s13g.winston.master;

import com.google.common.collect.Lists;
import com.s13g.winston.lib.core.util.concurrent.HttpRequester;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.simpleframework.http.Address;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the master container.
 */
public class MasterContainerTest {

  private HttpRequester mHttpRequester;
  private MasterContainer masterContainer;
  public TemporaryFolder folder;

  @Before
  public void initialize() {
    mHttpRequester = mock(HttpRequester.class);
  }

  private void initializeWithMap() throws IOException {
    when(mHttpRequester.requestUrl(anyString())).thenReturn("OK");
    masterContainer = new MasterContainer(0, new ArrayList<>(), mHttpRequester);
  }

  @Test
  public void testRequestValidNode() throws IOException {
    initializeWithMap();
  }
}
