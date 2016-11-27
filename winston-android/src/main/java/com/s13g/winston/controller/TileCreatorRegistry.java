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

import com.google.common.collect.ImmutableMap;
import com.s13g.winston.requests.ChannelValueRequester;
import com.s13g.winston.shared.ChannelType;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains all tile creators.
 */
public class TileCreatorRegistry {

  private final Map<ChannelType, ChannelTileCreator> mCreators;

  public TileCreatorRegistry(Context context, ChannelValueRequester requester) {
    mCreators = new HashMap<>();
    add(new NestThermostatTileCreator(context, requester));
    add(new WinstonSensorBoxTileCreator(context, requester));
    add(new WemoTileCreator(context, requester));
  }

  private void add(ChannelTileCreator creator) {
    if (mCreators.put(creator.getType(), creator) != null) {
      throw new RuntimeException("Duplicate creator for channel type: " + creator.getType().name());
    }
  }

  public Map<ChannelType, ChannelTileCreator> getCreators() {
    return ImmutableMap.copyOf(mCreators);
  }
}
