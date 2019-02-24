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
import com.s13g.winston.shared.data.Temperature;

import java.util.Optional;

/**
 * Handler for temperature sensor readings.
 */
public class TemperatureHandler implements Handler {
  private final TemperatureSensorController mController;
  private final NodePluginType mType;

  public TemperatureHandler(TemperatureSensorController temperatureSensorController,
                            NodePluginType type) {
    mController = temperatureSensorController;
    mType = type;
  }

  @Override
  public String handleRequest(String arguments) {
    // We only support a single temperature node right now.
    // TODO: Add support for multiple temperature nodes. Maybe even mix types.
    Optional<Temperature> temperatureOpt = mController.getTemperature();
    Optional<Integer> humidityOpt = mController.getHumidityPercent();
    // TODO: We need support some kind of exception with message here.
    String temperature = temperatureOpt.map(Temperature::toString).orElse("");
    String humidity = humidityOpt.map(Object::toString).orElse("");
    // TODO: Should output structured JSON data.
//    return temperature + "/" + humidity;
    return temperature;
  }

  @Override
  public NodePluginType getRpcName() {
    return mType;
  }
}
