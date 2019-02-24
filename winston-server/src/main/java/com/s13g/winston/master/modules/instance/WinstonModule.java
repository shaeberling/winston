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
import com.google.common.flogger.FluentLogger;
import com.s13g.winston.lib.winston.WinstonController;
import com.s13g.winston.lib.winston.WinstonGarageNodeController;
import com.s13g.winston.lib.winston.WinstonPowerNodeController;
import com.s13g.winston.lib.winston.WinstonSensorNodeController;
import com.s13g.winston.master.ModuleContext;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.instance.WinstonGarageNodeChannel;
import com.s13g.winston.master.channel.instance.WinstonPowerNodeChannel;
import com.s13g.winston.master.channel.instance.WinstonSensorNodeChannel;
import com.s13g.winston.master.modules.Module;
import com.s13g.winston.master.modules.ModuleCreationException;
import com.s13g.winston.master.modules.ModuleCreator;
import com.s13g.winston.master.modules.ModuleParameters;
import com.s13g.winston.master.modules.ModuleParameters.ChannelConfig;


import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Module that handles talking to Winston nodes.
 */
public class WinstonModule implements Module {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private static final String MODULE_TYPE = "winston";
  private static final String CHANNEL_TYPE_SENSOR = "sensors";
  private static final String CHANNEL_TYPE_POWER = "power";
  private static final String CHANNEL_TYPE_GARAGE = "garage";
  private static final String PARAM_TEMP_SENSOR = "temp-sensor";
  private static final String PARAM_SWITCH_IDS = "switch-ids";
  private static final String PARAM_DOOR_NAME = "door-name";
  private static final String PARAM_DOOR_RELAY = "door-relay";
  private static final String PARAM_DOOR_CLOSED_SENSOR = "door-closed-sensor";

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

    // SENSOR nodes.
    List<ChannelConfig> sensorChannels = params.getChannelsOfType(CHANNEL_TYPE_SENSOR);
    for (ChannelConfig sensorConfig : sensorChannels) {
      String address = sensorConfig.getAddress();
      if (Strings.isNullOrEmpty(address)) {
        throw new ModuleInitException("Sensor channel address may not be empty.");
      }
      Optional<List<String>> tempSensorsOpt = sensorConfig.getParam(PARAM_TEMP_SENSOR);
      if (!tempSensorsOpt.isPresent()) {
        log.atWarning().log("No 'temp-sensor' configured for Winston power channel.");
        continue;
      }
      WinstonSensorNodeController sensorNodeController =
          mWinstonController.getSensorNodeController(address);
      tempSensorsOpt.get().forEach(sensorNodeController::addTemperatureSensor);
      channels.add(new WinstonSensorNodeChannel(sensorNodeController));
    }

    // POWER nodes.
    List<ChannelConfig> powerChannels = params.getChannelsOfType(CHANNEL_TYPE_POWER);
    for (ChannelConfig powerConfig : powerChannels) {
      String address = powerConfig.getAddress();
      if (Strings.isNullOrEmpty(address)) {
        throw new ModuleInitException("Power channel address may not be empty.");
      }
      Optional<List<String>> powerSwitchesOpt = powerConfig.getParam(PARAM_SWITCH_IDS);
      if (!powerSwitchesOpt.isPresent()) {
        log.atWarning().log("No 'switch-ids' configured for Winston power channel.");
        continue;
      }
      WinstonPowerNodeController powerNodeController =
          mWinstonController.getPowerNodeController(address);
      powerSwitchesOpt.get().forEach(powerNodeController::addSwitch);
      channels.add(new WinstonPowerNodeChannel(powerNodeController));
    }

    // GARAGE nodes.
    List<ChannelConfig> garageChannels = params.getChannelsOfType(CHANNEL_TYPE_GARAGE);
    for (ChannelConfig garageConfig : garageChannels) {
      String address = garageConfig.getAddress();
      if (Strings.isNullOrEmpty(address)) {
        throw new ModuleInitException("Garage channel address may not be empty.");
      }
      Optional<List<String>> doorNamesOpt = garageConfig.getParam(PARAM_DOOR_NAME);
      if (!doorNamesOpt.isPresent()) {
        throw new ModuleInitException("No 'door-name' set for Winston power channel.");
      }
      Optional<List<String>> doorRelaysOpt = garageConfig.getParam(PARAM_DOOR_RELAY);
      if (!doorRelaysOpt.isPresent()) {
        throw new ModuleInitException("No 'door-relay' set for Winston power channel.");
      }
      Optional<List<String>> closedSensorsOpt = garageConfig.getParam(PARAM_DOOR_CLOSED_SENSOR);
      if (!closedSensorsOpt.isPresent()) {
        throw new ModuleInitException("No 'door-closed-sensor' set for Winston power channel.");
      }

      List<String> doorNames = doorNamesOpt.get();
      List<String> doorRelays = doorRelaysOpt.get();
      List<String> closedSensors = closedSensorsOpt.get();

      // Sanity check, we should have an equal amount of each.
      if (doorNames.size() != doorRelays.size() ||
          doorNames.size() != closedSensors.size()) {
        throw new ModuleInitException("Need equals number of door-name, door-relay and " +
            "door-closed-sensor entries in configuration.");
      }

      WinstonGarageNodeController garageNodeController =
          mWinstonController.getGarageNodeController(address);
      doorRelays.forEach(garageNodeController::addClicker);
      closedSensors.forEach(garageNodeController::addClosedState);
      channels.add(new WinstonGarageNodeChannel(doorNamesOpt.get(), garageNodeController));
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
    public WinstonModule create(ModuleContext context) {
      return new WinstonModule(getType(), context.getWinstonController());
    }
  }
}
