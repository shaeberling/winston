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

/**
 * Structure read-only data.
 */
public final class StructureData {
  public final String id;
  public final String name;
  public final AwayMode awayMode;
  public final ThermostatData[] mThermostatDatas;

  public StructureData(String id, String name, AwayMode awayMode, ThermostatData[]
      thermostatDatas) {
    this.id = id;
    this.name = name;
    this.awayMode = awayMode;
    this.mThermostatDatas = thermostatDatas;
  }
}
