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

import com.s13g.winston.lib.core.util.concurrent.HttpRequester;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.simpleframework.http.Address;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;
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

  HttpRequester mHttpRequester;
  MasterContainer masterContainer;

  @Before
  public void initialize() {
    mHttpRequester = mock(HttpRequester.class);
  }

  public void initializeWithMap() throws IOException {
    Map<String, String> nodeMap = new HashMap<>();
    nodeMap.put("node1", "http://node1.s13g.com:1234");
    nodeMap.put("foobar", "http://anothernode.s13g.com:4321");
    nodeMap.put("bar", "https://192.168.1.123:456");
    when(mHttpRequester.requestUrl(anyString())).thenReturn("OK");
    masterContainer = new MasterContainer(0, nodeMap, mHttpRequester);
  }

  @Test
  public void testRequestValidNode() throws IOException {
    initializeWithMap();

    // Mock the request side.
    Address address = mock(Address.class);
    when(address.toString()).thenReturn("/node1/io/relay/0/1");
    Request request = mock(Request.class);
    when(request.getAddress()).thenReturn(address);

    // Mock the response side.
    PrintStream printStream = mock(PrintStream.class);
    Response response = mock(Response.class);
    when(response.getPrintStream()).thenReturn(printStream);
    masterContainer.handle(request, response);

    verify(mHttpRequester).requestUrl(eq("http://node1.s13g.com:1234/io/relay/0/1"));
    verify(response).setStatus(Status.OK);
    verify(printStream).append(eq("OK"));
  }

  @Test
  public void testRequestAnotherValidNode() throws IOException {
    initializeWithMap();

    // Mock the request side.
    Address address = mock(Address.class);
    when(address.toString()).thenReturn("/bar/io/led/42");
    Request request = mock(Request.class);
    when(request.getAddress()).thenReturn(address);

    // Mock the response side.
    PrintStream printStream = mock(PrintStream.class);
    Response response = mock(Response.class);
    when(response.getPrintStream()).thenReturn(printStream);
    masterContainer.handle(request, response);

    verify(mHttpRequester).requestUrl(eq("https://192.168.1.123:456/io/led/42"));
    verify(response).setStatus(Status.OK);
    verify(printStream).append(eq("OK"));
  }

  @Test
  public void testInvalidRequestString() throws IOException {
    initializeWithMap();

    // Mock the request side.
    Address address = mock(Address.class);
    when(address.toString()).thenReturn("bar/io/led/42");
    Request request = mock(Request.class);
    when(request.getAddress()).thenReturn(address);

    // Mock the response side.
    PrintStream printStream = mock(PrintStream.class);
    Response response = mock(Response.class);
    when(response.getPrintStream()).thenReturn(printStream);

    masterContainer.handle(request, response);
    verify(response).setStatus(Status.BAD_REQUEST);
    verify(printStream, never()).append(any(CharSequence.class));
  }

  @Test
  public void testAnotherInvalidRequestString() throws IOException {
    initializeWithMap();

    // Mock the request side.
    Address address = mock(Address.class);
    when(address.toString()).thenReturn("/noMoreSlashes");
    Request request = mock(Request.class);
    when(request.getAddress()).thenReturn(address);

    // Mock the response side.
    PrintStream printStream = mock(PrintStream.class);
    Response response = mock(Response.class);
    when(response.getPrintStream()).thenReturn(printStream);

    masterContainer.handle(request, response);
    verify(response).setStatus(Status.BAD_REQUEST);
    verify(printStream, never()).append(any(CharSequence.class));
  }

  @Test
  public void testNonExistantNode() throws IOException {
    initializeWithMap();

    // Mock the request side.
    Address address = mock(Address.class);
    when(address.toString()).thenReturn("/iDontExist/io/led/42");
    Request request = mock(Request.class);
    when(request.getAddress()).thenReturn(address);

    // Mock the response side.
    PrintStream printStream = mock(PrintStream.class);
    Response response = mock(Response.class);
    when(response.getPrintStream()).thenReturn(printStream);

    masterContainer.handle(request, response);
    verify(response).setStatus(Status.NOT_FOUND);
    verify(printStream, never()).append(any(CharSequence.class));
  }

  @Test
  public void testIgnoreFavicon() throws IOException {
    initializeWithMap();

    // Mock the request side.
    Address address = mock(Address.class);
    when(address.toString()).thenReturn("/favicon.ico");
    Request request = mock(Request.class);
    when(request.getAddress()).thenReturn(address);

    // Mock the response side.
    PrintStream printStream = mock(PrintStream.class);
    Response response = mock(Response.class);
    when(response.getPrintStream()).thenReturn(printStream);

    masterContainer.handle(request, response);
    verify(response).setStatus(Status.OK);
    verify(printStream).append(eq(""));
  }
}
