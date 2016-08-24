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

import com.s13g.winston.lib.temperature.Temperature;

/**
 * Thermostat read-only values.
 */
public final class Thermostat {
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

  @Override
  public String toString() {
    String buffer =
        "Thermostat ID: " + id + "\n" +
            "Ambient Temp : " + ambientTemperature + "\n" +
            "Target Temp  : " + targetTemperature + "\n" +
            "HVAC State   : " + hvacState + "\n" +
            "Humidity     : " + humidity + "\n" +
            "===========================" + "\n";
    return buffer;
  }
}