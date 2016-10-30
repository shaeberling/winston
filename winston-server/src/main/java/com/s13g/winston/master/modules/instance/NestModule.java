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
import com.s13g.winston.lib.nest.NestController;
import com.s13g.winston.lib.nest.NestControllerFactory;
import com.s13g.winston.lib.nest.Thermostat;
import com.s13g.winston.lib.nest.data.ThermostatData;
import com.s13g.winston.master.ModuleContext;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.instance.NestThermostatChannel;
import com.s13g.winston.master.modules.Module;
import com.s13g.winston.master.modules.ModuleCreationException;
import com.s13g.winston.master.modules.ModuleCreator;
import com.s13g.winston.master.modules.ModuleParameters;
import com.s13g.winston.master.modules.ModuleParameters.ChannelConfig;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Module that handles communication with the Nest service.
 */
public class NestModule implements Module {
  private static final String MODULE_TYPE = "nest";
  private static final String CHANNEL_TYPE_NEST_SERVICE = "nest-service";
  private static final String PARAM_ACCESS_TOKEN = "access-token";

  private final String mType;
  private final NestControllerFactory mNestControllerFactory;

  private List<Channel> mChannels = ImmutableList.of();

  private NestModule(String type, NestControllerFactory nestControllerFactory) {
    mType = type;
    mNestControllerFactory = nestControllerFactory;
  }

  @Override
  public void initialize(ModuleParameters params) throws ModuleInitException {
    List<ChannelConfig> channelConfigs = params.getChannelsOfType(CHANNEL_TYPE_NEST_SERVICE);
    if (channelConfigs.size() != 1) {
      throw new ModuleInitException("Nest needs exactly one 'nest'service' to be configured.");
    }

    Optional<List<String>> accessTokenOpt = channelConfigs.get(0).getParam(PARAM_ACCESS_TOKEN);
    if (!accessTokenOpt.isPresent() || accessTokenOpt.get().size() != 1) {
      throw new ModuleInitException("Nest requires exactly one 'access-token'.");
    }
    NestController controller = mNestControllerFactory.create(accessTokenOpt.get().get(0));
    controller.refresh();
    ThermostatData[] thermostatData = controller.getThermostats();

    List<Channel> channels = new LinkedList<>();
    for (ThermostatData data : thermostatData) {
      channels.add(new NestThermostatChannel(data.id, new Thermostat(data.id, controller)));
    }
    mChannels = ImmutableList.copyOf(channels);
  }

  @Override
  public String getType() {
    return mType;
  }

  @Override
  public List<Channel> getChannels() {
    return mChannels;
  }

  public static class Creator implements ModuleCreator<NestModule> {
    @Override
    public String getType() {
      return MODULE_TYPE;
    }

    @Override
    public NestModule create(ModuleContext context) throws ModuleCreationException {
      return new NestModule(getType(), context.getNestControllerFactory());
    }
  }
}
