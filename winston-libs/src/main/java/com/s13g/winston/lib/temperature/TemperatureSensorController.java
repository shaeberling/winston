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

package com.s13g.winston.lib.temperature;

import com.s13g.winston.lib.plugin.NodeController;
import com.s13g.winston.shared.data.Temperature;

import java.util.Optional;

/**
 * Common temperature sensor interface.
 */
public interface TemperatureSensorController extends NodeController {
  Optional<Temperature> getTemperature();

  Optional<Integer> getHumidityPercent();
}
