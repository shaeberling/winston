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
import com.s13g.winston.shared.data.TypeConversion;
import com.s13g.winston.views.tiles.LightTileView;
import com.s13g.winston.views.tiles.TileWrapperView;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * For channel tile creators that produce light tiles.
 */
abstract class GenericLightTileCreator extends ChannelTileCreator {
  private final ChannelValueRequester mRequester;
  private final Executor postUpdateExecutor;

  GenericLightTileCreator(Context context, ChannelValueRequester requester) {
    super(context);
    mRequester = requester;
    postUpdateExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();

  }

  WrappedTileController createLightTile(final Channel channel,
                                        final ChannelValue value) {
    final Function<String, ListenableFuture<Boolean>> actionRequester =
        mRequester.getActionRequester(channel.moduleType, channel.id, value.id);
    LightTileView tile = new LightTileView(mContext);
    LightTileController controller =
        new LightTileController(tile, new Provider<Boolean>() {
          @Override
          public ListenableFuture<Boolean> provide() {
            return Futures.transform(
                mRequester.request(channel.moduleType, channel.id, value.id),
                new Function<byte[], Boolean>() {
                  @Override
                  public Boolean apply(@Nullable byte[] input) {
                    if (input == null) {
                      throw new NullPointerException("Temperature bytes are null");
                    }
                    try {
                      return TypeConversion.stringToBoolean(new String(input));
                    } catch (TypeConversion.IllegalFormatException e) {
                      throw new RuntimeException("Cannot convert to boolean", e);
                    }
                  }
                }, postUpdateExecutor);
          }
        }, new Function<Boolean, ListenableFuture<Boolean>>() {
          @Nullable
          @Override
          public ListenableFuture<Boolean> apply(@Nullable Boolean input) {
            return actionRequester.apply(TypeConversion.booleanToString(input));
          }
        });
    String title = isNullOrEmpty(channel.name) ? channel.id : channel.name;
    title += "/" + value.id;
    TileWrapperView wrapperView = wrapTile(tile, title);
    return new WrappedTileController(controller, wrapperView);
  }


}
