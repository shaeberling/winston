/*
 * Copyright 2015 The Winston Authors
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

package com.s13g.winston.tools.sauron;

import com.s13g.winston.common.ContainerServer;
import com.s13g.winston.common.io.DataLoader;
import com.s13g.winston.common.io.ResourceLoader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.simpleframework.http.Address;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for ImageServer
 */
public class ImageServerTest {
  private ContainerServer mMockServer;
  private ContainerServer.Creator mMockServerCreator;
  private Executor mImmediateExecutor;
  private ResourceLoader mMockResourceLoader;

  @Before
  public void initialize() {
    mMockServerCreator = (port, numThreads) -> mMockServer = mock(ContainerServer.class);
    mImmediateExecutor = Runnable::run;
    mMockResourceLoader = mock(ResourceLoader.class);
  }

  @Test
  public void testStartLoadsIndexPage() throws IOException {
    ImageServer imageServer = new ImageServer(123, mMockServerCreator, mImmediateExecutor,
        mMockResourceLoader);

    when(mMockResourceLoader.load(anyString())).thenReturn(new byte[]{1, 2, 23, 42});

    // Start serving should load the index file into memory once.
    imageServer.start();
    verify(mMockResourceLoader).load(anyString());
    verify(mMockServer).startServing(anyObject());
  }

  @Test
  public void testIndexPageLoadDoesntStart() throws IOException {
    ImageServer imageServer = new ImageServer(123, mMockServerCreator, mImmediateExecutor,
        mMockResourceLoader);

    when(mMockResourceLoader.load(anyString())).thenThrow(new IOException());

    imageServer.start();
    verify(mMockResourceLoader).load(anyString());
    // Make sure startServing is never called.
    verify(mMockServer, times(0)).startServing(anyObject());
  }

  @Test
  public void testServingUnmappedUrl() throws IOException {
    ImageServer imageServer = new ImageServer(123, mMockServerCreator, mImmediateExecutor,
        mMockResourceLoader);
    when(mMockResourceLoader.load(anyString())).thenReturn(new byte[]{1, 2, 23, 42});
    imageServer.start();

    ArgumentCaptor<Container> containerCaptor = ArgumentCaptor.forClass(Container.class);
    verify(mMockServer).startServing(containerCaptor.capture());

    Response mockResponse = mock(Response.class);
    // Simulate request to unavailable address.
    containerCaptor.getValue().handle(createMockRequestForUrl("doesNotExist"), mockResponse);
    verify(mockResponse).setStatus(Status.NOT_FOUND);
  }

  @Test
  public void testServingIndexHtml() throws IOException {
    ImageServer imageServer = new ImageServer(123, mMockServerCreator, mImmediateExecutor,
        mMockResourceLoader);
    when(mMockResourceLoader.load(anyString())).thenReturn(new byte[]{1, 2, 23, 42});
    imageServer.start();

    ArgumentCaptor<Container> containerCaptor = ArgumentCaptor.forClass(Container.class);
    verify(mMockServer).startServing(containerCaptor.capture());

    OutputStream mockOutputStream = mock(OutputStream.class);
    Response mockResponse = mock(Response.class);
    when(mockResponse.getOutputStream()).thenReturn(mockOutputStream);

    // Simulate request to unavailable address.
    containerCaptor.getValue().handle(createMockRequestForUrl("/"), mockResponse);
    verify(mockResponse).setStatus(Status.OK);
    verify(mockResponse).setContentType("text/html");
    verify(mockResponse).close();
    verify(mockOutputStream).write(eq(new byte[]{1, 2, 23, 42}));
    verify(mockOutputStream).close();
  }

  @Test
  public void testServingImageFileNotSet() throws IOException {
    ImageServer imageServer = new ImageServer(123, mMockServerCreator, mImmediateExecutor,
        mMockResourceLoader);
    when(mMockResourceLoader.load(anyString())).thenReturn(new byte[]{1, 2, 23, 42});
    imageServer.start();

    ArgumentCaptor<Container> containerCaptor = ArgumentCaptor.forClass(Container.class);
    verify(mMockServer).startServing(containerCaptor.capture());

    OutputStream mockOutputStream = mock(OutputStream.class);
    Response mockResponse = mock(Response.class);
    when(mockResponse.getOutputStream()).thenReturn(mockOutputStream);

    // Simulate request to image URL..
    containerCaptor.getValue().handle(createMockRequestForUrl("/now.jpg"), mockResponse);
    verify(mockResponse).setStatus(Status.OK);
    verify(mockResponse).setContentType("image/jpeg");
    verify(mockResponse).close();

    // We never set the image
    verify(mockOutputStream).write(eq(new byte[0]));
    verify(mockOutputStream).close();
  }

  @Test
  public void testServingImageFileSet() throws IOException {
    ImageServer imageServer = new ImageServer(123, mMockServerCreator, mImmediateExecutor,
        mMockResourceLoader);
    when(mMockResourceLoader.load(anyString())).thenReturn(new byte[]{1, 2, 23, 42});
    imageServer.start();
    imageServer.setCurrentFile(() -> Optional.of(new byte[]{1, 2, 3, 5, 8, 13, 21}));

    ArgumentCaptor<Container> containerCaptor = ArgumentCaptor.forClass(Container.class);
    verify(mMockServer).startServing(containerCaptor.capture());

    OutputStream mockOutputStream = mock(OutputStream.class);
    Response mockResponse = mock(Response.class);
    when(mockResponse.getOutputStream()).thenReturn(mockOutputStream);

    // Simulate request to image URL..
    containerCaptor.getValue().handle(createMockRequestForUrl("/now.jpg"), mockResponse);
    verify(mockResponse).setStatus(Status.OK);
    verify(mockResponse).setContentType("image/jpeg");
    verify(mockResponse).close();

    // We never set the image
    verify(mockOutputStream).write(eq(new byte[]{1, 2, 3, 5, 8, 13, 21}));
    verify(mockOutputStream).close();
  }

  @Test
  public void testServingThrowsException() throws IOException {
    ImageServer imageServer = new ImageServer(123, mMockServerCreator, mImmediateExecutor,
        mMockResourceLoader);
    when(mMockResourceLoader.load(anyString())).thenReturn(new byte[]{1, 2, 23, 42});
    imageServer.start();

    ArgumentCaptor<Container> containerCaptor = ArgumentCaptor.forClass(Container.class);
    verify(mMockServer).startServing(containerCaptor.capture());

    OutputStream mockOutputStream = mock(OutputStream.class);
    Response mockResponse = mock(Response.class);
    doThrow(new IOException()).when(mockOutputStream).write(any());
    when(mockResponse.getOutputStream()).thenReturn(mockOutputStream);

    containerCaptor.getValue().handle(createMockRequestForUrl("/"), mockResponse);
    // Even though it throws, response should be closed.
    verify(mockResponse).close();
  }

  @Test
  public void testResponseCloseThrows() throws IOException {
    ImageServer imageServer = new ImageServer(123, mMockServerCreator, mImmediateExecutor,
        mMockResourceLoader);
    when(mMockResourceLoader.load(anyString())).thenReturn(new byte[]{1, 2, 23, 42});
    imageServer.start();

    ArgumentCaptor<Container> containerCaptor = ArgumentCaptor.forClass(Container.class);
    verify(mMockServer).startServing(containerCaptor.capture());

    OutputStream mockOutputStream = mock(OutputStream.class);
    Response mockResponse = mock(Response.class);
    doThrow(new IOException()).when(mockResponse).close();
    when(mockResponse.getOutputStream()).thenReturn(mockOutputStream);

    containerCaptor.getValue().handle(createMockRequestForUrl("/"), mockResponse);
    // Nothing horrible should happen if response.close throws.
  }

  @Test
  public void testErrorWhenSettingCurrentFile() throws IOException {
    ImageServer imageServer = new ImageServer(123, mMockServerCreator, mImmediateExecutor,
        mMockResourceLoader);
    when(mMockResourceLoader.load(anyString())).thenReturn(new byte[]{1, 2, 23, 42});
    imageServer.start();
    imageServer.setCurrentFile(Optional::empty);
  }

  private static Request createMockRequestForUrl(String url) {
    Address mockAddress = mock(Address.class);
    when(mockAddress.toString()).thenReturn(url);
    Request mockRequest = mock(Request.class);
    when(mockRequest.getAddress()).thenReturn(mockAddress);
    return mockRequest;
  }
}
