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

package com.s13g.winston.master.channel.instance;

import com.google.common.collect.ImmutableList;
import com.s13g.winston.lib.nest.Thermostat;
import com.s13g.winston.lib.temperature.Temperature;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.ChannelException;
import com.s13g.winston.master.channel.ChannelValue;
import com.s13g.winston.master.channel.ReadOnlyChannelValue;

import java.util.List;
import java.util.Optional;

/**
 * A channel for a Nest thermostat.
 */
public class NestThermostatChannel implements Channel {
  /**
   * After this time, the data is considered old and needs to be refreshed if accessed again. This
   * avoid multiple requests to the multiple values here to send out a request each, even though
   * we get the data for all channels with one request to the Nest API.
   */
  private static final long MAX_DATA_AGE_MILLIS = 10 * 1000;
  private final String mChannelId;
  private final Thermostat mThermostat;

  public NestThermostatChannel(String channelId, Thermostat thermostat) {
    mChannelId = channelId;
    mThermostat = thermostat;
  }

  @Override
  public String getChannelId() {
    return mChannelId;
  }

  @Override
  public List<ChannelValue> getValues() {
    ChannelValue<String> deviceName = new ReadOnlyChannelValue<>(
        "deviceName", this::readName, "Cannot set name");
    ChannelValue<Float> ambientTempCelcius = new ReadOnlyChannelValue<>(
        "ambientTempCelsius", this::readTemperature,
        "Cannot set ambientTemp. Set targetTemp instead.");
    ChannelValue<Float> humidity = new ReadOnlyChannelValue<>(
        "humidity", this::readHumidity, "Cannot set humidity.");

    return ImmutableList.of(
        deviceName,
        ambientTempCelcius,
        humidity,
        new NestTargetTemperatureCelsiusChannel());
  }

  private String readName() throws ChannelException {
    Optional<String> deviceName = mThermostat.refresh(MAX_DATA_AGE_MILLIS).getName();
    if (!deviceName.isPresent()) {
      throw new ChannelException("Cannot read thermostat name for '" + mChannelId + "'.");
    }
    return deviceName.get();
  }

  /** Reads the temperature from the thermostat. */
  private float readTemperature() throws ChannelException {
    Optional<Temperature> ambientTemperature =
        mThermostat.refresh(MAX_DATA_AGE_MILLIS).getAmbientTemperature();
    if (!ambientTemperature.isPresent()) {
      throw new ChannelException("Cannot read ambient temperature for '" + mChannelId + "'.");
    }
    return ambientTemperature.get().get(Temperature.Unit.CELSIUS);
  }

  /** Read the humidity from the thermostat. */
  private float readHumidity() throws ChannelException {
    Optional<Float> humidity = mThermostat.refresh(MAX_DATA_AGE_MILLIS).getHumidity();
    if (!humidity.isPresent()) {
      throw new ChannelException("Cannot read humidity for '" + mChannelId + "'.");
    }
    return humidity.get();
  }

  /** Read/Write channel for the thermostat's target temperature. */
  private class NestTargetTemperatureCelsiusChannel implements ChannelValue<Float> {
    @Override
    public Mode getType() {
      return Mode.READ_WRITE;
    }

    @Override
    public String getName() {
      return "targetTempCelsius";
    }

    @Override
    public void writeRaw(String valueRaw) throws ChannelException {
      try {
        write(Float.parseFloat(valueRaw));
      } catch (NumberFormatException ex) {
        throw new ChannelException("Cannot parse temperature", ex);
      }
    }

    @Override
    public void write(Float value) throws ChannelException {
      mThermostat.setTargetTemperature(new Temperature(value, Temperature.Unit.CELSIUS));
    }

    @Override
    public Float read() throws ChannelException {
      Optional<Temperature> ambientTemperature =
          mThermostat.refresh(MAX_DATA_AGE_MILLIS).getTargetTemperature();
      if (!ambientTemperature.isPresent()) {
        throw new ChannelException("Cannot read target temperature for '" + mChannelId + "'.");
      }
      return ambientTemperature.get().get(Temperature.Unit.CELSIUS);
    }
  }
}
