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

package com.s13g.winston.master.channel.instance;

import com.google.common.collect.ImmutableList;
import com.s13g.winston.lib.nest.Structure;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.ChannelException;
import com.s13g.winston.master.channel.ChannelValue;
import com.s13g.winston.master.channel.ReadOnlyChannelValue;

import java.util.List;
import java.util.Optional;

/**
 * A channel for a Nest structure.
 */
public class NestStructureChannel implements Channel {
  /**
   * After this time, the data is considered old and needs to be refreshed if accessed again. This
   * avoid multiple requests to the multiple values here to send out a request each, even though
   * we get the data for all channels with one request to the Nest API.
   */
  private static final long MAX_DATA_AGE_MILLIS = 10 * 1000;
  private final String mChannelId;
  private final Structure mStructure;

  public NestStructureChannel(String channelId, Structure structure) {
    mChannelId = channelId;
    mStructure = structure;
  }

  @Override
  public String getChannelId() {
    return mChannelId;
  }

  @Override
  public List<ChannelValue> getValues() {
    return ImmutableList.of(
        new ReadOnlyChannelValue<>("structureName", this::readName, "Cannot set structure name."));
  }

  private String readName() throws ChannelException {
    Optional<String> structureName = mStructure.refresh(MAX_DATA_AGE_MILLIS).getName();
    if (!structureName.isPresent()) {
      throw new ChannelException("Cannot read structure name for '" + mChannelId + "'.");
    }
    return structureName.get();
  }
}
