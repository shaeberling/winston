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

package com.s13g.winston.lib.nest;

import com.s13g.winston.lib.nest.data.NestResponseParser;

/**
 * Produces NestControllers.
 */
public class NestControllerFactory {
  private final NestResponseParser mResponseParser;

  public NestControllerFactory(NestResponseParser responseParser) {
    mResponseParser = responseParser;
  }

  /**
   * Creates a nest controller with the given accessToken for authentication.
   */
  public NestController create(String accessToken) {
    return new NestControllerImpl("Bearer " + accessToken, mResponseParser);
  }
}
