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

package com.s13g.winston.master;

import com.s13g.winston.lib.nest.NestControllerFactory;
import com.s13g.winston.lib.nest.data.NestResponseParser;
import com.s13g.winston.lib.tv.TvControllerFactory;
import com.s13g.winston.lib.wemo.WemoController;
import com.s13g.winston.lib.wemo.WemoControllerImpl;
import com.s13g.winston.lib.winston.WinstonController;

import java.util.Base64;
import java.util.concurrent.Executors;

/**
 * Contains classes needed my some or all modules.
 */
public class ModuleContext {
  private final WemoController mWemoController;
  private final TvControllerFactory mTvControllerFactory;
  private final NestControllerFactory mNestControllerFactory;
  private final WinstonController mWinstonController;

  ModuleContext() {
    Base64.Encoder base64Encoder = Base64.getEncoder();
    NestResponseParser nestResponseParser = new NestResponseParser();

    mWemoController = new WemoControllerImpl();
    mTvControllerFactory =
        new TvControllerFactory(base64Encoder, Executors.newSingleThreadExecutor());
    mNestControllerFactory = new NestControllerFactory(nestResponseParser);
    mWinstonController = new WinstonController();
  }

  public WemoController getWemoController() {
    return mWemoController;
  }

  public TvControllerFactory getTvControllerFactory() {
    return mTvControllerFactory;
  }

  public NestControllerFactory getNestControllerFactory() {
    return mNestControllerFactory;
  }

  public WinstonController getWinstonController() {
    return mWinstonController;
  }
}
