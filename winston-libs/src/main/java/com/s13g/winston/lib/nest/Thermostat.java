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
import com.s13g.winston.shared.data.Temperature;

import java.util.Optional;

/**
 * A nest thermostat that can be queried to the latest data.
 */
public class Thermostat {
  private final String mId;
  private final NestController mNestController;

  private boolean mLastRefreshSuccess;
  private long mLastRefreshTime;
  private ThermostatData mLatestData;

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
        mLatestData = data;
        mLastRefreshSuccess = true;
        mLastRefreshTime = System.currentTimeMillis();
        break;
      }
    }
    return this;
  }

  public Optional<String> getName() {
    return returnIfRefreshSuccessful(mLatestData.name);
  }

  public Optional<Temperature> getAmbientTemperature() {
    return returnIfRefreshSuccessful(mLatestData.ambientTemperature);
  }

  public Optional<Temperature> getTargetTemperature() {
    return returnIfRefreshSuccessful(mLatestData.targetTemperature);
  }

  public boolean setTargetTemperature(Temperature temperature) {
    return mNestController.setTemperature(mId, temperature);
  }

  public Optional<Float> getHumidity() {
    return returnIfRefreshSuccessful((float) mLatestData.humidity);
  }

  public Optional<HvacState> getHvacState() {
    return returnIfRefreshSuccessful(mLatestData.hvacState);
  }

  public Optional<Boolean> getIsOnline() {
    return returnIfRefreshSuccessful(mLatestData.isOnline);
  }

  private <T> Optional<T> returnIfRefreshSuccessful(T value) {
    if (!mLastRefreshSuccess) {
      return Optional.empty();
    }
    return Optional.of(value);
  }
}
