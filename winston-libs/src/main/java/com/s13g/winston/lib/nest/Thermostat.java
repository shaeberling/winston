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

import com.s13g.winston.lib.nest.data.HvacState;
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
  private long mLastRefreshTime;
  private String mName;
  private Temperature mAmbientTemperature;
  private Temperature mTargetTemperature;
  private float mHumidty;
  private HvacState mHvacState;

  public Thermostat(String id, NestController nestController) {
    mId = id;
    mNestController = nestController;
  }

  /**
   * Refreshes the thermostat if the last refresh is older than the given age.
   *
   * @param maxAgeMillis if the thermostat data has been refreshed less than this time ago, it will
   * not be refreshed again.
   */
  public Thermostat refresh(long maxAgeMillis) {
    boolean dataExpired = mLastRefreshTime + maxAgeMillis < System.currentTimeMillis();
    if (maxAgeMillis < 0 || !dataExpired) {
      return this;
    }
    mNestController.refresh();
    mLastRefreshSuccess = false;
    for (ThermostatData data : mNestController.getThermostats()) {
      if (data.id.equals(mId)) {
        mName = data.name;
        mAmbientTemperature = data.ambientTemperature;
        mTargetTemperature = data.targetTemperature;
        mHumidty = (float) data.humidity;
        mHvacState = data.hvacState;
        mLastRefreshSuccess = true;
        mLastRefreshTime = System.currentTimeMillis();
        break;
      }
    }
    return this;
  }

  public Optional<String> getName() {
    return returnIfRefreshSuccessful(mName);
  }

  public Optional<Temperature> getAmbientTemperature() {
    return returnIfRefreshSuccessful(mAmbientTemperature);
  }

  public Optional<Temperature> getTargetTemperature() {
    return returnIfRefreshSuccessful(mTargetTemperature);
  }

  public void setTargetTemperature(Temperature temperature) {
    mNestController.setTemperature(mId, temperature);
  }

  public Optional<Float> getHumidity() {
    return returnIfRefreshSuccessful(mHumidty);
  }

  public Optional<HvacState> getHvacState() {
    return returnIfRefreshSuccessful(mHvacState);
  }

  private <T> Optional<T> returnIfRefreshSuccessful(T value) {
    if (!mLastRefreshSuccess) {
      return Optional.empty();
    }
    return Optional.of(value);
  }
}
