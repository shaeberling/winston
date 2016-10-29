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

package com.s13g.winston.master;

import com.s13g.winston.common.RequestHandler;
import com.s13g.winston.common.RequestHandlingException;
import com.s13g.winston.lib.core.util.concurrent.HttpRequester;
import com.s13g.winston.proto.Master.MasterConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

/**
 * HTTP Container for serving the master daemon HTTP requests.
 */
public class MasterContainer implements Container {

  private static final Logger LOG = LogManager.getLogger(MasterContainer.class);
  private final int mPort;
  private final HttpRequester mHttpRequester;
  private final List<RequestHandler> mRequestHandlers;

  MasterContainer(int port, List<RequestHandler> requestHandlers, HttpRequester httpRequester) {
    mPort = port;
    mHttpRequester = httpRequester;
    mRequestHandlers = requestHandlers;
  }

  /**
   * Starts serving from this container.
   *
   * @param numThreads the number of threads to handle the HTTP requests.
   * @throws IOException thrown if HTTP serving could not be started.
   */
  void startServing(int numThreads) throws IOException {
    final ContainerSocketProcessor processor = new ContainerSocketProcessor(this, numThreads);

    // Since this server will run forever, no need to close connection.
    final Connection connection = new SocketConnection(processor);
    final SocketAddress address = new InetSocketAddress(mPort);
    connection.connect(address);
    LOG.info("Listening to: " + address.toString());
  }

  @Override
  public void handle(Request request, Response response) {
    try {
      response.getPrintStream().append(doHandle(request));
      response.setStatus(Status.OK);
    } catch (RequestHandlingException e) {
      LOG.warn("Cannot handle request", e);
      if (e.errorCode.isPresent()) {
        response.setStatus(e.errorCode.get());
      } else {
        response.setStatus(Status.BAD_REQUEST);
      }
    } catch (Exception e) {
      LOG.error("Error handling request", e);
    } finally {
      try {
        response.close();
      } catch (IOException e) {
        LOG.warn("Cannot close response", e);
      }
    }
  }

  /**
   * Handle the HTTP request
   *
   * @param req the HTTP request
   * @return The response of the HTTP request.
   * @throws RequestHandlingException thrown if the request could not be handled.
   */
  private String doHandle(Request req) throws RequestHandlingException {
    String requestUrl = req.getAddress().toString();
    // Ignore this, don't even log it.
    if (requestUrl.equals("/favicon.ico")) {
      return "";
    }
    LOG.info("Request: " + requestUrl);

    if (!requestUrl.startsWith("/")) {
      throw new RequestHandlingException("Cannot handle request: " + requestUrl);
    }

    // Remove slash prefix.
    requestUrl = requestUrl.substring(1);

    // TODO: This should be done on a background thread, with a proper queue, de-duping per
    // command/node etc.
    for (RequestHandler handler : mRequestHandlers) {
      if (handler.canHandle(requestUrl)) {
        return handler.doHandle(requestUrl);
      }
    }
    throw new RequestHandlingException("No request handler found. " + requestUrl, Status.NOT_FOUND);
  }
}
