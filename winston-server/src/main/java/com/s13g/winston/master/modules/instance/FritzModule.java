/*
 * Copyright 2019 The Winston Authors
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
import com.s13g.winston.master.ModuleContext;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.modules.Module;
import com.s13g.winston.master.modules.ModuleCreator;
import com.s13g.winston.master.modules.ModuleParameters;

import java.util.List;

public class FritzModule implements Module {
  private static final String MODULE_TYPE = "fritz";
  private static final String CHANNEL_TYPE = "fritz-controller";

  private final String mType;
  private List<Channel> mChannels = ImmutableList.of();

  private FritzModule(String type) {
    mType = type;
  }

  @Override
  public void initialize(ModuleParameters params) throws ModuleInitException {
    List<ModuleParameters.ChannelConfig> channelConfigs = params.getChannelsOfType(CHANNEL_TYPE);
    if (channelConfigs.size() != 1) {
      throw new ModuleInitException("Fritz needs exactly one 'fritz controller' to be configured.");
    }


  }

  @Override
  public String getType() {
    return mType;
  }

  @Override
  public List<Channel> getChannels() {
    return mChannels;
  }

  public static class Creator implements ModuleCreator<FritzModule> {
    @Override
    public String getType() {
      return MODULE_TYPE;
    }

    @Override
    public FritzModule create(ModuleContext context) {
      return new FritzModule(getType());
    }
  }
}
