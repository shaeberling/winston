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

import com.google.common.flogger.FluentLogger;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.s13g.winston.net.HttpRequester;
import com.s13g.winston.proto.nano.ForClients.ChannelData;

import java.io.IOException;
import java.util.concurrent.Executor;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Makes a request to the master to get a list of all channels and its values.
 */
@ParametersAreNonnullByDefault
public class ChannelDataRequester implements AutoCloseable {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  private static final String CHANNEL_DATA_PATH = "/channeldata";
  private final HttpRequester mRequester;
  private final Executor mExecutor;
  private volatile boolean mIsClosed;

  public ChannelDataRequester(HttpRequester requester, Executor executor) {
    mRequester = checkNotNull(requester);
    mExecutor = checkNotNull(executor);
    mIsClosed = false;
  }

  public ListenableFuture<ChannelData> execute() {
    final SettableFuture<ChannelData> result = SettableFuture.create();
    mExecutor.execute(new Runnable() {
      @Override
      public void run() {

        if (mIsClosed) {
          return;
        }
        try {
          log.atInfo().log("Making request to channel data path");
          byte[] response = mRequester.request(CHANNEL_DATA_PATH);
          log.atInfo().log("Response received ... " + response.length);
          ChannelData channelData = ChannelData.parseFrom(response);
          if (!mIsClosed) {
            result.set(channelData);
          }
        } catch (IOException e) {
          if (!mIsClosed) {
            result.setException(e);
          }
        }
      }
    });
    return result;
  }

  @Override
  public void close() throws Exception {
    mIsClosed = true;
  }
}
