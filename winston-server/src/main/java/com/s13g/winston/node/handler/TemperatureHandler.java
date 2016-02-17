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

package com.s13g.winston.node.handler;

import com.s13g.winston.lib.plugin.NodePluginType;
import com.s13g.winston.lib.temperature.TemperatureSensorController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handler for temperature sensor readings.
 */
public class TemperatureHandler implements Handler {
  private static final Logger LOG = LogManager.getLogger(TemperatureHandler.class);
  private final TemperatureSensorController mController;

  public TemperatureHandler(TemperatureSensorController temperatureSensorController) {
    mController = temperatureSensorController;
  }

  @Override
  public String handleRequest(String arguments) {
    // We only support a single temperature node right now.
    // TODO: Add support for multiple temperature nodes. Maybe even mix types.
    return mController.getTemperature().toString();
  }

  @Override
  public NodePluginType getRpcName() {
    return NodePluginType.DS18B20_TEMP;
  }
}
