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

package com.s13g.winston.master.modules.types;

import com.s13g.winston.common.ActionFailedException;
import com.s13g.winston.lib.temperature.Temperature;
import com.s13g.winston.master.modules.Module;

/**
 * Interface for modules handling temperatures/thermostats.
 */
public interface TemperatureModule extends Module {

  /**
   * Gets the current temperature of this module.
   *
   * @return The current tempeature.
   * @throws ActionFailedException if the temperature could not be determined.
   */
  Temperature getTemperature() throws ActionFailedException;

  /**
   * Sets the new temperature. Only call this if it is supported.
   *
   * @param temperature the new temperature.
   * @throws ActionFailedException if the new temperature cannot be set
   */
  void setTemperature(Temperature temperature) throws ActionFailedException;

  /**
   * @return The current relative humidity 0..1.
   * @throws ActionFailedException if the humidity cannot be read.
   */
  double getHumidity() throws ActionFailedException;

  /** Whether this module supports setting the temperature. */
  boolean isSupportingSettingTemperature();

  /** Whether this module supports humidity read-out. */
  boolean isSupportingHumidity();


}
