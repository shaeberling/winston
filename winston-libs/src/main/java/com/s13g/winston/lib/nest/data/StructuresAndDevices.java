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

package com.s13g.winston.lib.nest.data;

import com.google.common.base.Optional;
import com.s13g.winston.lib.temperature.Temperature;

/**
 * Contains the response for all structures and devices.
 */
public class StructuresAndDevices {
  public final Structure[] structures;

  public StructuresAndDevices(Structure[] structures) {
    this.structures = structures;
  }

  public static class Structure {
    public final String name;
    public final AwayMode awayMode;
    public final Thermostat[] thermostats;

    public Structure(String name, AwayMode awayMode, Thermostat[] thermostats) {
      this.name = name;
      this.awayMode = awayMode;
      this.thermostats = thermostats;
    }
  }

  public static class Thermostat {
    public final String id;
    public final String name;
    public final double humidity;
    public final String softwareVersion;
    public final Temperature ambientTemperature;
    public final Temperature targetTemperature;
    public final boolean isOnline;
    public final HvacState hvacState;

    public Thermostat(String id, String name, double humidity, String softwareVersion, Temperature
        ambientTemperature, Temperature targetTemperature, boolean isOnline, HvacState hvacState) {
      this.id = id;
      this.name = name;
      this.humidity = humidity;
      this.softwareVersion = softwareVersion;
      this.ambientTemperature = ambientTemperature;
      this.targetTemperature = targetTemperature;
      this.isOnline = isOnline;
      this.hvacState = hvacState;
    }
  }

  public enum AwayMode {
    HOME("home"), AWAY("away"), AUTO_AWAY("auto-away");
    public final String str;

    AwayMode(String str) {
      this.str = str;
    }

    /**
     * Gets the mode enum from the given value string.
     */
    public static Optional<AwayMode> fromString(String modeStr) {
      for (AwayMode mode : AwayMode.values()) {
        if (mode.str.equals(modeStr)) {
          return Optional.of(mode);
        }
      }
      return Optional.absent();
    }
  }

  /**
   * Gets the state enum from the given value string.
   */
  public enum HvacState {
    HEATING("heating"), COOLING("cooling"), OFF("off");
    public final String str;

    HvacState(String str) {
      this.str = str;
    }

    /**
     * Gets the state enum from the given value string.
     */
    public static Optional<HvacState> fromString(String stateStr) {
      for (HvacState state : HvacState.values()) {
        if (state.str.equals(stateStr)) {
          return Optional.of(state);
        }
      }
      return Optional.absent();
    }
  }
}
