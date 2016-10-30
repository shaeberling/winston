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

import com.s13g.winston.lib.core.util.Pair;
import com.s13g.winston.lib.temperature.Temperature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Parses Nest JSON responses.
 */
public class NestResponseParser {
  private static final Logger LOG = Logger.getLogger("NestResponseParser");

  public Structure[] parseStructureAndDevicesResponse(String json) {
    JSONObject root = new JSONObject(json);

    try {
      // For some members like 'devices', structures' etc they are an object and not an array,
      // which is odd. It's possible that this turns into an array if multiple devices are
      // present. Might need to revisit this code in this case.
      JSONObject devices = root.getJSONObject("devices");
      JSONObject thermostats = devices.getJSONObject("thermostats");

      List<JSONObject> jsonThermostats = new ArrayList<>();
      thermostats.keys().forEachRemaining(
          (key) -> jsonThermostats.add(thermostats.getJSONObject(key)));
      LOG.info("Thermostats found: " + jsonThermostats.size());

      Map<String, ThermostatData> thermostatMap = new HashMap<>();
      for (JSONObject jsonThermostat : jsonThermostats) {
        ThermostatData thermostatData = parseThermostat(jsonThermostat);
        thermostatMap.put(thermostatData.id, thermostatData);
      }

      List<Pair<String, JSONObject>> jsonStructures = new ArrayList<>();
      JSONObject structures = root.getJSONObject("structures");
      structures.keys().forEachRemaining(
          (key) -> jsonStructures.add(new Pair<>(key, structures.getJSONObject(key))));
      Structure[] structureResult = new Structure[jsonStructures.size()];
      for (int i = 0; i < jsonStructures.size(); ++i) {
        structureResult[i] = parseStructure(jsonStructures.get(i).first,
            jsonStructures.get(i).second, thermostatMap);
      }
      return structureResult;
    } catch (JSONException ex) {
      return new Structure[0];
    }
  }

  private Structure parseStructure(String structureId,
                                   JSONObject jsonStructure,
                                   Map<String, ThermostatData> thermostatMap) {
    String name = jsonStructure.getString("name");
    JSONArray jsonThermostats = jsonStructure.getJSONArray("thermostats");
    Optional<AwayMode> awayMode = AwayMode.fromString(jsonStructure.getString("away"));
    if (!awayMode.isPresent()) {
      throw new RuntimeException("Cannot parse away mode.");
    }

    ThermostatData[] thermostatDatas = new ThermostatData[jsonThermostats.length()];
    for (int i = 0; i < jsonThermostats.length(); ++i) {
      String thermostatId = jsonThermostats.getString(i);
      if (!thermostatMap.containsKey(thermostatId)) {
        throw new RuntimeException("Thermostat ID '" + thermostatId + "' not found. Referenced " +
            "from structure '" + name + "'.");
      }
      thermostatDatas[i] = thermostatMap.get(thermostatId);
    }
    return new Structure(structureId, name, awayMode.get(), thermostatDatas);
  }

  private ThermostatData parseThermostat(JSONObject jsonThermostat) {
    String deviceId = jsonThermostat.getString("device_id");
    String name = jsonThermostat.getString("name");
    double humidity = jsonThermostat.getDouble("humidity");
    String softwareVersion = jsonThermostat.getString("software_version");
    Temperature ambientTemp = new Temperature((float) jsonThermostat.getDouble
        ("ambient_temperature_c"), Temperature.Unit.CELSIUS);
    Temperature targetTemp = new Temperature((float) jsonThermostat.getDouble
        ("target_temperature_c"), Temperature.Unit.CELSIUS);
    boolean isOnline = jsonThermostat.getBoolean("is_online");
    java.util.Optional<HvacState> hvacState = HvacState.fromString(jsonThermostat.getString
        ("hvac_state"));

    if (!hvacState.isPresent()) {
      throw new RuntimeException("Unable to obtain HVAC state");
    }
    return new ThermostatData(deviceId, name, humidity, softwareVersion, ambientTemp,
        targetTemp, isOnline, hvacState.get());
  }
}
