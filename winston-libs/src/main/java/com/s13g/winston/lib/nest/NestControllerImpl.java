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

import com.google.common.base.Preconditions;
import com.s13g.winston.lib.core.net.HttpUtil;
import com.s13g.winston.lib.nest.data.AwayMode;
import com.s13g.winston.lib.nest.data.NestResponseParser;
import com.s13g.winston.lib.nest.data.Structure;
import com.s13g.winston.lib.nest.data.Thermostat;
import com.s13g.winston.lib.temperature.Temperature;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/** Default implementation of the Nest controller. */
public class NestControllerImpl implements NestController {
  private static final Logger LOG = Logger.getLogger("NestControllerImpl");
  private static final String ROOT_URL = "https://developer-api.nest.com/";
  private final String mAuthHeader;
  private final NestResponseParser mResponseParser;
  /** Maps thermostat ID to thermostat. */
  private Map<String, Thermostat> mThermostats = new HashMap<>();
  /** Maps thermostat ID to the structure it is contained in. */
  private Map<String, Structure> mStructures = new HashMap<>();

  public NestControllerImpl(String authHeader, NestResponseParser responseParser) {
    mAuthHeader = authHeader;
    mResponseParser = responseParser;
  }

  @Override
  public boolean refresh() {
    mThermostats.clear();
    mStructures.clear();
    String result;
    try {
      result = HttpUtil.requestUrl(ROOT_URL, HttpUtil.Method.GET, HttpUtil.ContentType.JSON,
          mAuthHeader);
    } catch (IOException ex) {
      LOG.log(Level.WARNING, "Cannot load data", ex);
      return false;
    }
    LOG.info("Get all devices response:\n" + result);

    Structure[] structures = mResponseParser.parseStructureAndDevicesResponse(result);
    for (Structure structure : structures) {
      for (Thermostat thermostat : structure.thermostats) {
        if (mThermostats.put(thermostat.id, thermostat) != null) {
          throw new RuntimeException("Duplicate thermostat: " + thermostat.id);
        }
        if (mStructures.put(thermostat.id, structure) != null) {
          throw new RuntimeException("Duplicate structure for thermostat: " + thermostat.id);
        }
      }
    }
    return true;
  }

  @Override
  public Thermostat[] getThermostats() {
    return mThermostats.values().toArray(new Thermostat[0]);
  }

  @Override
  public boolean setTemperature(String thermostatId, Temperature temperature) {
    float tempC = roundToNearestHalf(temperature.get(Temperature.Unit.CELSIUS));
    String url = ROOT_URL + "devices/thermostats/" + thermostatId;
    String data = "{\"target_temperature_c\": " + tempC + "}";
    try {
      String result = HttpUtil.requestUrl(url, HttpUtil.Method.PUT, HttpUtil.ContentType.JSON,
          mAuthHeader, Optional.of(data));
      LOG.info("Result from changing temperature: " + result);
      // TODO: Check result to ensure setting temperature succeeded.
    } catch (IOException ex) {
      LOG.log(Level.WARNING, "Error changing temperature", ex);
      return false;
    }
    return true;
  }

  @Override
  public boolean setAwayMode(String thermostatId, AwayMode awayMode) {
    Structure structure = mStructures.get(thermostatId);
    if (structure == null) {
      throw new RuntimeException("No structure found for thermostat: " + thermostatId);
    }
    String structureId = structure.id;
    String url = ROOT_URL + "structures/" + structureId;
    try {
      String data = "{\"away\": \"" + awayMode.str + "\"}";
      String result = HttpUtil.requestUrl(url, HttpUtil.Method.PUT, HttpUtil.ContentType.JSON,
          mAuthHeader, Optional.of(data));
      LOG.info("Result from setting away state: " + result);
      return true;
    } catch (IOException ex) {
      LOG.log(Level.WARNING, "Error changing away mode", ex);
    }
    return false;
  }

  /** Nest only support half-increments, so this is rounding to the nearest. */
  private static float roundToNearestHalf(float temp) {
    return Math.round(temp * 2) / 2f;
  }
}
