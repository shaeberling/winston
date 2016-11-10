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

package com.s13g.winston;

import com.google.common.base.Strings;
import com.s13g.winston.common.RequestHandler;
import com.s13g.winston.common.RequestHandlingException;
import com.s13g.winston.proto.Master.AuthenticatedClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.http.Address;
import org.simpleframework.http.Status;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Contains all handlers that can handle incoming HTTP requests.
 */
public class RequestHandlers {
  private static final Logger LOG = LogManager.getLogger(RequestHandlers.class);
  private static final String AUTH_TOKEN_PARAM = "authtoken";

  private final Map<String, AuthenticatedClient> mAuthClients;
  private final List<RequestHandler> mRequestHandlers;
  private final Object mLock;

  public RequestHandlers(List<AuthenticatedClient> authClientList) {
    mAuthClients = createAuthClientMap(authClientList);
    mRequestHandlers = new LinkedList<>();
    mLock = new Object();
  }

  public void addRequestHandler(RequestHandler requestHandler) {
    synchronized (mLock) {
      mRequestHandlers.add(requestHandler);
    }
  }

  /**
   * Handles the given request and performs an authentication check.
   *
   * @param address the request address.
   * @param response where the response is written to.
   * @throws RequestHandlingException      if there was an error while trying to handle this
   *                                       request.
   * @throws RequestNotAuthorizedException if the client was not authorized to perform this
   *                                       request.
   */
  public void handleRequest(Address address, OutputStream response)
      throws RequestHandlingException, RequestNotAuthorizedException {
    String path = address.getPath().getPath();
    if (path.startsWith("/")) {
      path = path.substring(1);
    }

    String authToken = address.getQuery().get(AUTH_TOKEN_PARAM);
    if (Strings.isNullOrEmpty(authToken)) {
      throw new RequestNotAuthorizedException("No authtoken given");
    }
    if (!mAuthClients.containsKey(authToken)) {
      throw new RequestNotAuthorizedException("Invalid authtoken");
    }
    LOG.info("Authorized client: " + mAuthClients.get(authToken).getName());
    handleRequestTrusted(path, response);
  }

  /** Call this for requests that are already trusted and don't need an auth token check. */
  public void handleRequestTrusted(String requestUrl, OutputStream response) throws
      RequestHandlingException {
    synchronized (mLock) {
      // TODO: This should be done on a background thread, with a proper queue, de-duping per
      // command/node etc.
      for (RequestHandler handler : mRequestHandlers) {
        if (handler.canHandle(requestUrl)) {
          handler.doHandle(requestUrl, response);
          return;
        }
      }
      throw new RequestHandlingException("No request handler found. " + requestUrl,
          Status.NOT_FOUND);
    }
  }

  private Map<String, AuthenticatedClient> createAuthClientMap(List<AuthenticatedClient> clients) {
    Map<String, AuthenticatedClient> result = new HashMap<>();
    for (AuthenticatedClient client : clients) {
      result.put(client.getAuthToken(), client);
    }
    return result;
  }

  public static class RequestNotAuthorizedException extends Exception {
    RequestNotAuthorizedException(String message) {
      super(message);
    }
  }
}
