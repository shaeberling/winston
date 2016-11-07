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

import com.s13g.winston.common.RequestHandler;
import com.s13g.winston.common.RequestHandlingException;

import org.simpleframework.http.Status;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains all handlers that can handle incoming HTTP requests.
 */
public class RequestHandlers {
  private final List<RequestHandler> mRequestHandlers;
  private final Object mLock;

  public RequestHandlers() {
    mRequestHandlers = new LinkedList<>();
    mLock = new Object();
  }

  public void addRequestHandler(RequestHandler requestHandler) {
    synchronized (mLock) {
      mRequestHandlers.add(requestHandler);
    }
  }

  public String handleRequest(String requestUrl) throws RequestHandlingException {
    synchronized (mLock) {
      // TODO: This should be done on a background thread, with a proper queue, de-duping per
      // command/node etc.
      for (RequestHandler handler : mRequestHandlers) {
        if (handler.canHandle(requestUrl)) {
          return handler.doHandle(requestUrl);
        }
      }
      throw new RequestHandlingException("No request handler found. " + requestUrl,
          Status.NOT_FOUND);
    }
  }
}
