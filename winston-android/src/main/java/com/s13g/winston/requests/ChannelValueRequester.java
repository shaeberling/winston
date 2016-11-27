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

package com.s13g.winston.requests;

import com.google.common.util.concurrent.ListenableFuture;
import com.s13g.winston.net.HttpRequester;

import java.util.Locale;
import java.util.concurrent.Executor;

/**
 * Enables requests for a particular channel.
 */
public class ChannelValueRequester {
  private static final String URL_PATTERN = "/io/%s/%s/%s";
  private final HttpRequester mHttpRequester;
  private final Executor mExecutor;

  public ChannelValueRequester(HttpRequester httpRequester, Executor executor) {
    mHttpRequester = httpRequester;
    mExecutor = executor;
  }

  public ListenableFuture<byte[]> request(String module, String channel, String value) {
    String requestUrl = String.format(Locale.US, URL_PATTERN, module, channel, value);
    return mHttpRequester.requestAsync(requestUrl, mExecutor);
  }
}
