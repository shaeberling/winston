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
import com.google.common.util.concurrent.SettableFuture;
import com.s13g.winston.proto.nano.ForClients.SystemData;

import java.util.concurrent.Executor;

/**
 * SystemDataLoader that can be used to test without an actual server.
 */
public class SystemDataLoaderForTesting implements SystemDataLoader {
  private final Executor mExecutor;

  public SystemDataLoaderForTesting(Executor executor) {
    mExecutor = executor;
  }

  @Override
  public ListenableFuture<SystemData> loadSystemData() {

    final SettableFuture<SystemData> future = SettableFuture.create();
    mExecutor.execute(new Runnable() {
      @Override
      public void run() {
        // Creating test data instead of making a real request.
        SystemData data = new SystemData();
        data.success = true;
        data.ioChannel = new SystemData.IoChannel[4];

        data.ioChannel[0] = new SystemData.IoChannel();
        data.ioChannel[0].id = "/pi-power-1/io/relay/1";
        data.ioChannel[0].type = "switch/light";
        data.ioChannel[0].name = "Living room light (front)";

        data.ioChannel[1] = new SystemData.IoChannel();
        data.ioChannel[1].id = "/pi-cam-1/io/ds18b20_temp/0";
        data.ioChannel[1].type = "temperature";
        data.ioChannel[1].name = "Living room temperature";

        data.ioChannel[2] = new SystemData.IoChannel();
        data.ioChannel[2].id = "/pi-garage/io/relay/0";
        data.ioChannel[2].type = "tapswitch/garage";
        data.ioChannel[2].name = "Garage 1";

        data.ioChannel[3] = new SystemData.IoChannel();
        data.ioChannel[3].id = "/pi-garage/io/relay/1";
        data.ioChannel[3].type = "tapswitch/garage";
        data.ioChannel[3].name = "Garage 2";
        future.set(data);
      }
    });
    return future;
  }

  @Override
  public void close() throws Exception {
    // Nothing to be done in this test version, since we don't make realy network requests.
  }
}
