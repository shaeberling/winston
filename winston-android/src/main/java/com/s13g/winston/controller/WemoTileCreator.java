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

import com.s13g.winston.proto.nano.ForClients.ChannelData.Channel;
import com.s13g.winston.proto.nano.ForClients.ChannelData.Channel.ChannelValue;
import com.s13g.winston.requests.ChannelValueRequester;
import com.s13g.winston.shared.ChannelType;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Creates tiles for the wemo switch module.
 */
class WemoTileCreator extends GenericLightTileCreator {

  WemoTileCreator(Context context, ChannelValueRequester requester) {
    super(context, requester);
  }

  @Override
  ChannelType getType() {
    return ChannelType.WEMO_SWITCH;
  }

  @Nonnull
  @Override
  public List<WrappedTileController> createWrappedTiles(Channel channel) {
    List<WrappedTileController> mViews = new LinkedList<>();
    for (ChannelValue value : channel.value) {
      // TODO: Need to add a 'type' to each channel value so we know what it is.
      if (value.id.startsWith("switch")) {
        mViews.add(createLightTile(channel, value));
      }
    }
    return mViews;
  }
}
