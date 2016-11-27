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
import android.view.View;

import com.s13g.winston.proto.nano.ForClients.ChannelData.Channel;
import com.s13g.winston.shared.ChannelType;
import com.s13g.winston.views.tiles.TileWrapperView;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Interface for all tile controllers.
 */
public abstract class ChannelTileCreator {
  final Context mContext;

  ChannelTileCreator(Context context) {
    mContext = context;
  }

  /**
   * @return The type of the channel this controller can create tiles for.
   */
  abstract ChannelType getType();

  /**
   * Creates and returns all wrapped tiles for this channel.
   *
   * @param channel the channel for which to create tiles for.
   * @return All the controllers which contains the views and functionality to control them.
   */
  @Nonnull
  public abstract List<WrappedTileController> createWrappedTiles(Channel channel);

  TileWrapperView wrapTile(View view, String title) {
    TileWrapperView tileWrapper = new TileWrapperView(mContext);
    tileWrapper.setTileView(title, view);
    return tileWrapper;
  }
}
