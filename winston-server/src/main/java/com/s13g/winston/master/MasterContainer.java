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

import com.google.common.flogger.FluentLogger;
import com.s13g.winston.RequestHandlers;
import com.s13g.winston.common.RequestHandlingException;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.net.ssl.SSLContext;

/**
 * HTTP Container for serving the master daemon HTTP requests.
 */
public class MasterContainer implements Container {

  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  private final int mPort;
  private final RequestHandlers mRequestHandlers;

  MasterContainer(int port, RequestHandlers requestHandlers) {
    mPort = port;
    mRequestHandlers = requestHandlers;
  }

  /**
   * Starts serving HTTPS encrypted traffic from this container.
   *
   * @param numThreads the number of threads to handle the HTTP requests.
   * @param context a valid SSLContext for serving secure connections.
   * @throws IOException thrown if HTTP serving could not be started.
   */
  void startServing(int numThreads, SSLContext context) throws IOException {
    final ContainerSocketProcessor processor = new ContainerSocketProcessor(this, numThreads);

    // Since this server will run forever, no need to close connection.
    final Connection connection = new SocketConnection(processor);
    final SocketAddress address = new InetSocketAddress(mPort);
    connection.connect(address, context);
    log.atInfo().log("Listening to: " + address.toString());
  }

  @Override
  public void handle(Request request, Response response) {
    try {
      doHandle(request, response.getOutputStream());
      response.setStatus(Status.OK);
    } catch (RequestHandlingException e) {
      log.atWarning().log("Cannot handle request: %s", e.getMessage());
      if (e.errorCode.isPresent()) {
        response.setStatus(e.errorCode.get());
      } else {
        response.setStatus(Status.BAD_REQUEST);
      }
    } catch (RequestHandlers.RequestNotAuthorizedException e) {
      log.atWarning().log("Unauthorized access: %s", request.getAddress().toString());
      response.setStatus(Status.FORBIDDEN);
      try {
        response.getPrintStream().append(e.getMessage());
      } catch (IOException e1) {
        log.atSevere().withCause(e1).log("Cannot write error to response");
      }
    } catch (Exception e) {
      log.atSevere().withCause(e).log("Error handling request");
    } finally {
      try {
        response.close();
      } catch (IOException e) {
        log.atWarning().withCause(e).log("Cannot close response");
      }
    }
  }

  /**
   * Handle the HTTP request
   *
   * @param req the HTTP request.
   * @param response where the HTTP response is written to.
   * @throws RequestHandlingException thrown if the request could not be handled.
   */
  private void doHandle(Request req, OutputStream response)
      throws RequestHandlingException, RequestHandlers.RequestNotAuthorizedException {
    String requestPath = req.getAddress().getPath().getPath();
    // Ignore this, don't even log it.
    if (requestPath.equals("/favicon.ico")) {
      return;
    }
    log.atInfo().log("Request Path: " + requestPath);

    if (!requestPath.startsWith("/")) {
      throw new RequestHandlingException("Cannot handle request: " + requestPath);
    }

    // Remove slash prefix.
    mRequestHandlers.handleRequest(req.getAddress(), response);
  }
}
