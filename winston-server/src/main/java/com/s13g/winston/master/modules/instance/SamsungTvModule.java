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

package com.s13g.winston.master.modules.instance;

import com.google.common.collect.ImmutableList;
import com.s13g.winston.lib.tv.TvControllerFactory;
import com.s13g.winston.master.ModuleContext;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.instance.SamsungTvChannel;
import com.s13g.winston.master.modules.Module;
import com.s13g.winston.master.modules.ModuleCreationException;
import com.s13g.winston.master.modules.ModuleCreator;
import com.s13g.winston.master.modules.ModuleParameters;
import com.s13g.winston.master.modules.ModuleParameters.ChannelConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Module for interacting with Samsung TVs.
 */
public class SamsungTvModule implements Module {
  private static final Logger LOG = LogManager.getLogger(SamsungTvModule.class);
  private static final String MODULE_TYPE = "samsungtv";
  private static final String CHANNEL_TYPE_TV = "tv";

  private final String mType;
  private final TvControllerFactory mFactory;

  private List<Channel> mChannels = ImmutableList.of();

  private SamsungTvModule(String type, TvControllerFactory factory) {
    mType = type;
    mFactory = factory;
  }

  @Override
  public void initialize(ModuleParameters params) throws ModuleInitException {
    List<ChannelConfig> channelConfigs = params.getChannelsOfType(CHANNEL_TYPE_TV);
    if (channelConfigs.isEmpty()) {
      throw new ModuleInitException("No TVs configured");
    }

    List<Channel> channels = new LinkedList<>();
    for (ChannelConfig channelConfig : channelConfigs) {
      String tvIp = channelConfig.getAddress();
      channels.add(new SamsungTvChannel(tvIp, mFactory.forSamsungTv(tvIp)));
    }
    mChannels = ImmutableList.copyOf(channels);
    LOG.info("Channels: " + mChannels.size());
  }

  @Override
  public String getType() {
    return mType;
  }

  @Override
  public List<Channel> getChannels() {
    return mChannels;
  }

  public static class Creator implements ModuleCreator<SamsungTvModule> {

    @Override
    public String getType() {
      return MODULE_TYPE;
    }

    @Override
    public SamsungTvModule create(ModuleContext context) throws ModuleCreationException {
      return new SamsungTvModule(getType(), context.getTvControllerFactory());
    }
  }
}
