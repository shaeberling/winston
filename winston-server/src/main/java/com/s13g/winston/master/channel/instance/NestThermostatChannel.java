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

import java.util.List;
import java.util.Optional;

/**
 * A channel for a Nest thermostat.
 */
public class NestThermostatChannel implements Channel {
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
    return ImmutableList.of(
        new NestAmbientTemperatureCelsiusChannel(),
        new NestTargetTemperatureCelsiusChannel(),
        new NestHumidityChannel());
  }

  private class NestAmbientTemperatureCelsiusChannel implements ChannelValue<Float> {

    @Override
    public Mode getType() {
      return Mode.READ_ONLY;
    }

    @Override
    public String getName() {
      return "ambientTempCelsius";
    }

    @Override
    public void writeRaw(String value) throws ChannelException {
      throw new ChannelException("Cannot set ambientTemp. Set targetTemp instead.");
    }

    @Override
    public void write(Float value) throws ChannelException {
      throw new ChannelException("Cannot set ambientTemp. Set targetTemp instead.");
    }

    @Override
    public Float read() throws ChannelException {
      Optional<Temperature> ambientTemperature = mThermostat.refresh().getAmbientTemperature();
      if (!ambientTemperature.isPresent()) {
        throw new ChannelException("Cannot read ambient temperature for '" + mChannelId + "'.");
      }
      return ambientTemperature.get().get(Temperature.Unit.CELSIUS);
    }
  }

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
      Optional<Temperature> ambientTemperature = mThermostat.refresh().getTargetTemperature();
      if (!ambientTemperature.isPresent()) {
        throw new ChannelException("Cannot read target temperature for '" + mChannelId + "'.");
      }
      return ambientTemperature.get().get(Temperature.Unit.CELSIUS);
    }
  }

  private class NestHumidityChannel implements ChannelValue<Float> {
    @Override
    public Mode getType() {
      return Mode.READ_ONLY;
    }

    @Override
    public String getName() {
      return "humidity";
    }

    @Override
    public void writeRaw(String valueRaw) throws ChannelException {
      throw new ChannelException("Cannot set humidity.");
    }

    @Override
    public void write(Float value) throws ChannelException {
      throw new ChannelException("Cannot set humidity.");
    }

    @Override
    public Float read() throws ChannelException {
      Optional<Float> ambientTemperature = mThermostat.refresh().getHumidity();
      if (!ambientTemperature.isPresent()) {
        throw new ChannelException("Cannot read humidity for '" + mChannelId + "'.");
      }
      return ambientTemperature.get();
    }
  }
}
