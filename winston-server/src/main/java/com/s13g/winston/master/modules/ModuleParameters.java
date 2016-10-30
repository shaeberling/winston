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

package com.s13g.winston.master.modules;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.s13g.winston.proto.Master;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A set of parameters that is given to a module on creation.
 */
public class ModuleParameters {
  private final Map<String, List<ChannelConfig>> mChannels;

  ModuleParameters(List<Master.Channel> channels) {
    mChannels = new HashMap<>();
    for (Master.Channel channel : channels) {
      if (!mChannels.containsKey(channel.getType())) {
        mChannels.put(channel.getType(), new LinkedList<>());
      }
      mChannels.get(channel.getType()).add(new ChannelConfig(
          channel.getType(),
          channel.getAddress(),
          channel.getParameterList()));
    }
  }

  /** Returns all the channels of the given type. */
  public List<ChannelConfig> getChannelsOfType(String channelType) {
    if (!mChannels.containsKey(channelType)) {
      return ImmutableList.of();
    }
    return ImmutableList.copyOf(mChannels.get(channelType));
  }

  public static class ChannelConfig {
    private final String mType;
    private final String mAddress;
    private final Map<String, List<String>> mParameters;

    ChannelConfig(String type, String address, List<Master.Parameter> parameters) {
      mType = type;
      mAddress = address;
      Map<String, List<String>> tempParameters = new HashMap<>();
      for (Master.Parameter parameter : parameters) {
        if (!tempParameters.containsKey(parameter.getName())) {
          tempParameters.put(parameter.getName(), new LinkedList<>());
        }
        tempParameters.get(parameter.getName()).add(parameter.getValue());
      }
      mParameters = ImmutableMap.copyOf(tempParameters);
    }

    /**
     * @param name the name of the parameter
     * @return If present, the value of the parameter, otherwise empty.
     */
    public Optional<List<String>> getParam(String name) {
      return Optional.ofNullable(mParameters.get(name));
    }

    public String getType() {
      return mType;
    }

    public String getAddress() {
      return mAddress;
    }
  }
}
