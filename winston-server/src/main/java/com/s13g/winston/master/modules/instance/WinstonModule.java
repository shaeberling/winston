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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.s13g.winston.lib.winston.WinstonController;
import com.s13g.winston.lib.winston.WinstonPowerNodeController;
import com.s13g.winston.lib.winston.WinstonSensorNodeController;
import com.s13g.winston.master.ModuleContext;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.instance.WinstonPowerNodeChannel;
import com.s13g.winston.master.channel.instance.WinstonSensorNodeChannel;
import com.s13g.winston.master.modules.Module;
import com.s13g.winston.master.modules.ModuleCreationException;
import com.s13g.winston.master.modules.ModuleCreator;
import com.s13g.winston.master.modules.ModuleParameters;
import com.s13g.winston.master.modules.ModuleParameters.ChannelConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Module that handles talking to Winston nodes.
 */
public class WinstonModule implements Module {
  private static Logger LOG = LogManager.getLogger(WinstonModule.class);

  private static final String MODULE_TYPE = "winston";
  private static final String CHANNEL_TYPE_SENSOR = "sensors";
  private static final String CHANNEL_TYPE_POWER = "power";
  private static final String CHANNEL_TYPE_GARAGE = "garage";
  private static final String PARAM_TEMP_SENSOR = "temp-sensor";
  private static final String PARAM_SWITCH_IDS = "switch-ids";
  private static final String PARAM_DOOR_NAME = "door-name";
  private static final String PARAM_DOOR_RELAY = "door-relay";

  private final String mType;
  private final WinstonController mWinstonController;

  private List<Channel> mChannels = ImmutableList.of();

  private WinstonModule(String type, WinstonController winstonController) {
    mType = type;
    mWinstonController = winstonController;
  }

  @Override
  public void initialize(ModuleParameters params) throws ModuleInitException {
    List<Channel> channels = new LinkedList<>();

    List<ChannelConfig> sensorChannels = params.getChannelsOfType(CHANNEL_TYPE_SENSOR);
    for (ChannelConfig sensorConfig : sensorChannels) {
      String address = sensorConfig.getAddress();
      if (Strings.isNullOrEmpty(address)) {
        throw new ModuleInitException("Sensor channel address may not be empty.");
      }
      Optional<List<String>> tempSensorsOpt = sensorConfig.getParam(PARAM_TEMP_SENSOR);
      if (!tempSensorsOpt.isPresent()) {
        LOG.warn("No 'temp-sensor' configured for Winston power channel.");
        continue;
      }
      WinstonSensorNodeController sensorNodeController =
          mWinstonController.getSensorNodeController(address);
      tempSensorsOpt.get().forEach(sensorNodeController::addTemperatureSensor);
      channels.add(new WinstonSensorNodeChannel(sensorNodeController));
    }

    List<ChannelConfig> powerChannels = params.getChannelsOfType(CHANNEL_TYPE_POWER);
    for (ChannelConfig powerConfig : powerChannels) {
      String address = powerConfig.getAddress();
      if (Strings.isNullOrEmpty(address)) {
        throw new ModuleInitException("Power channel address may not be empty.");
      }
      Optional<List<String>> powerSwitchesOpt = powerConfig.getParam(PARAM_SWITCH_IDS);
      if (!powerSwitchesOpt.isPresent()) {
        LOG.warn("No 'switch-ids' configured for Winston power channel.");
        continue;
      }
      WinstonPowerNodeController powerNodeController =
          mWinstonController.getPowerNodeController(address);
      powerSwitchesOpt.get().forEach(powerNodeController::addSwitch);
      channels.add(new WinstonPowerNodeChannel(powerNodeController));
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

  public static class Creator implements ModuleCreator<WinstonModule> {

    @Override
    public String getType() {
      return MODULE_TYPE;
    }

    @Override
    public WinstonModule create(ModuleContext context) throws ModuleCreationException {
      return new WinstonModule(getType(), context.getWinstonController());
    }
  }
}
