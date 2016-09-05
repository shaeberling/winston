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

package com.s13g.winston.common;

/**
 * Classes implementing this interface can respond to HTTP requests.
 */
public interface RequestHandler {
  /**
   * Handle the HTTP request
   *
   * @param request the request
   * @return The response for the request.
   * @throws RequestHandlingException Thrown is there was an error handling the request.
   */
  String doHandle(String request) throws RequestHandlingException;

  /**
   * @param request the request
   * @return Whether this handler is able to handle this request.
   */
  boolean canHandle(String request);
}
