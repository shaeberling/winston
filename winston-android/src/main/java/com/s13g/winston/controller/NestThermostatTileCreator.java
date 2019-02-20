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

import android.content.Context;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.s13g.winston.async.Provider;
import com.s13g.winston.proto.nano.ForClients.ChannelData.Channel;
import com.s13g.winston.proto.nano.ForClients.ChannelData.Channel.ChannelValue;
import com.s13g.winston.requests.ChannelValueRequester;
import com.s13g.winston.shared.ChannelType;
import com.s13g.winston.shared.data.Temperature;
import com.s13g.winston.views.tiles.TemperatureTileView;
import com.s13g.winston.views.tiles.TileWrapperView;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Creates the tiles for Nest thermostat channels.
 */
class NestThermostatTileCreator extends ChannelTileCreator {
  private final ChannelValueRequester mRequester;
  // FIXME.
  private final Executor postUpdateExecutor;

  NestThermostatTileCreator(Context context, ChannelValueRequester requester) {
    super(context);
    mRequester = requester;
    postUpdateExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
  }

  @Override
  public ChannelType getType() {
    return ChannelType.NEST_THERMOSTAT;
  }

  @Override
  @Nonnull
  public List<WrappedTileController> createWrappedTiles(Channel channel) {
    List<WrappedTileController> mViews = new LinkedList<>();
    for (ChannelValue value : channel.value) {
      if (value.id.equals("ambientTempCelsius")) {
        mViews.add(createTemperatureTile(channel, value));
      }
    }
    return mViews;
  }

  private WrappedTileController createTemperatureTile(
      final Channel channel, final ChannelValue value) {
    TemperatureTileView tile = new TemperatureTileView(mContext);
    TemperatureTileController tempController =
        new TemperatureTileController(tile, new Provider<Temperature>() {
          @Override
          public ListenableFuture<Temperature> provide() {
            return Futures.transform(
                mRequester.request(channel.moduleType, channel.id, value.id),
                new Function<byte[], Temperature>() {
                  @Override
                  public Temperature apply(@Nullable byte[] input) {
                    if (input == null) {
                      throw new NullPointerException("Temperature bytes are null");
                    }

                    // Will throw NFE if it cannot parse the result.
                    float tempC = Float.parseFloat(new String(input));
                    // The result by the winston sensor box is always celsius.
                    return new Temperature(tempC, Temperature.Unit.CELSIUS);
                  }
                }, postUpdateExecutor);
          }
        });
    String title = isNullOrEmpty(channel.name) ? channel.id : channel.name;
    title += "/" + value.id;
    TileWrapperView wrapperView = wrapTile(tile, title);
    return new WrappedTileController(tempController, wrapperView);
  }
}
