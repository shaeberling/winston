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

import com.s13g.winston.lib.temperature.TemperatureSensorController.Temperature;

/**
 * Contains the response for all structures and devices.
 */
public class StructuresAndDevices {
  public final Thermostat[] thermostats;

  public StructuresAndDevices(Thermostat[] thermostats) {
    this.thermostats = thermostats;
  }

  public static class Thermostat {
    public final String name;
    public final float humidity;
    public final String softwareVersion;
    public final Temperature ambientTemperature;
    public final Temperature targetTemperature;
    public final boolean isOnline;
    public final HvacState hvacState;
    public final Structure containedInStructure;


    public Thermostat(String name, float humidity, String softwareVersion, Temperature
        ambientTemperature, Temperature targetTemperature, boolean isOnline, HvacState hvacState,
                      Structure containedInStructure) {
      this.name = name;
      this.humidity = humidity;
      this.softwareVersion = softwareVersion;
      this.ambientTemperature = ambientTemperature;
      this.targetTemperature = targetTemperature;
      this.isOnline = isOnline;
      this.hvacState = hvacState;
      this.containedInStructure = containedInStructure;
    }
  }

  public static class Structure {
    public final AwayMode awayMode;

    public Structure(AwayMode awayMode) {
      this.awayMode = awayMode;
    }
  }

  public enum AwayMode {
    HOME("home"), AWAY("away"), AUTO_AWAY("auto-away");
    public final String str;

    AwayMode(String str) {
      this.str = str;
    }
  }

  public enum HvacState {
    HEATING("heating"), COOLING("cooling"), OFF("off");
    public final String str;

    HvacState(String str) {
      this.str = str;
    }
  }
}
