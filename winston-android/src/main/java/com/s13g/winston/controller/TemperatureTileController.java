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

package com.s13g.winston.controller;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.s13g.winston.async.Provider;
import com.s13g.winston.shared.data.Temperature;
import com.s13g.winston.views.tiles.TemperatureTileView;

import javax.annotation.Nullable;

/**
 * Controller for the temperature tile.
 */
class TemperatureTileController extends BasicTileController<TemperatureTileView, Temperature> {

  TemperatureTileController(TemperatureTileView tileView, Provider<Temperature> provider) {
    super(tileView, provider, new Function<Temperature, ListenableFuture<Boolean>>() {
      @Nullable
      @Override
      public ListenableFuture<Boolean> apply(@Nullable Temperature input) {
        // We don't have actions for temperature tiles yet.
        return Futures.immediateFuture(true);
      }
    });
  }

  @Override
  protected Temperature updateValueOnMainAction(Temperature currentValue) {
    return currentValue;
  }
}
