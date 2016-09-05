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

package com.s13g.winston.net;

import com.google.common.util.concurrent.ListenableFuture;
import com.s13g.winston.proto.nano.ForClients.SystemData;

import java.util.concurrent.Executor;

/**
 * Default implementation for loading system data.
 */
public class SystemDataLoaderImpl implements SystemDataLoader {
  private final Executor mExecutor;

  /**
   * Creates a new system data loader.
   *
   * @param executor the executor to be used for making the request to load the data. master server
   * being available.
   */
  public SystemDataLoaderImpl(Executor executor) {
    mExecutor = executor;
  }

  @Override
  public ListenableFuture<SystemData> loadSystemData() {
    throw new RuntimeException("Not implemented yet");
  }

  @Override
  public void close() throws Exception {
    throw new RuntimeException("Not implemented yet");
  }
}
