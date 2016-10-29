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

package com.s13g.winston.lib.clitests;

import com.s13g.winston.lib.tv.TvController;
import com.s13g.winston.lib.tv.TvControllerFactory;

import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * CLI to test TV remote functionality.
 */
public class TvRemotetestCli {
  public static void main(String[] args) {
    Base64.Encoder base64Encoder = Base64.getEncoder();
    Executor tvExecutor = Executors.newSingleThreadExecutor();

    TvControllerFactory factory = new TvControllerFactory(base64Encoder, tvExecutor);
    TvController tvController = factory.forSamsungTv("192.168.1.107");

    tvController.switchOff();
  }
}
