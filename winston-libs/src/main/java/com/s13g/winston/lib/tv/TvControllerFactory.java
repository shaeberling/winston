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

package com.s13g.winston.lib.tv;

import java.util.Base64;
import java.util.concurrent.Executor;

/**
 * Creates TvControllers.
 */
public class TvControllerFactory {
  private static final String REMOTE_NAME = "Winston";
  private final Base64.Encoder mBase64Encoder;
  private final Executor mRequestExecutor;

  public TvControllerFactory(Base64.Encoder base64Encoder, Executor requestExecutor) {
    mBase64Encoder = base64Encoder;
    mRequestExecutor = requestExecutor;
  }

  /**
   * Creates a TvController for a Samsung TV at the given IP address.
   */
  public TvController forSamsungTv(String ipAddress) {
    SamsungRemote samsungRemote = new SamsungRemote(REMOTE_NAME, ipAddress, mBase64Encoder);
    return new SamsungTvController(samsungRemote, mRequestExecutor);
  }
}
