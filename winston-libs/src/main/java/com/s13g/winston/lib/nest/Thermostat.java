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

package com.s13g.winston.lib.nest;

import com.s13g.winston.lib.nest.data.ThermostatData;
import com.s13g.winston.lib.temperature.Temperature;

import java.util.Optional;

/**
 * A nest thermostat that can be queried to the latest data.
 */
public class Thermostat {
  private final String mId;
  private final NestController mNestController;

  private boolean mLastRefreshSuccess;
  private Temperature mAmbientTemperature;
  private Temperature mTargetTemperature;
  private float mHumidty;

  public Thermostat(String id, NestController nestController) {
    mId = id;
    mNestController = nestController;
  }

  public Thermostat refresh() {
    mNestController.refresh();
    mLastRefreshSuccess = false;
    for (ThermostatData data : mNestController.getThermostats()) {
      if (data.id.equals(mId)) {
        mAmbientTemperature = data.ambientTemperature;
        mTargetTemperature = data.targetTemperature;
        mHumidty = (float) data.humidity;
        mLastRefreshSuccess = true;
        break;
      }
    }
    return this;
  }

  public Optional<Temperature> getAmbientTemperature() {
    if (!mLastRefreshSuccess) {
      return Optional.empty();
    }
    return Optional.of(mAmbientTemperature);
  }

  public Optional<Temperature> getTargetTemperature() {
    if (!mLastRefreshSuccess) {
      return Optional.empty();
    }
    return Optional.of(mTargetTemperature);
  }

  public void setTargetTemperature(Temperature temperature) {
    mNestController.setTemperature(mId, temperature);
  }

  public Optional<Float> getHumidity() {
    if (!mLastRefreshSuccess) {
      return Optional.empty();
    }
    return Optional.of(mHumidty);
  }
}
