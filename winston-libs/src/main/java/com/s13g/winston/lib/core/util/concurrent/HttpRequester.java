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

package com.s13g.winston.lib.core.util.concurrent;

import java.io.IOException;

/**
 * Interface for classes that make HTTP requests.
 */
public interface HttpRequester {

  /**
   * Makes a request to the given URL.
   *
   * @param rpcUrl the give HTTP URL
   * @return Response received from the request.
   */
  String requestUrl(String rpcUrl) throws IOException;
}
