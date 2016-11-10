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
import com.s13g.winston.shared.data.Temperature;
import com.s13g.winston.lib.winston.WinstonSensorNodeController;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.ChannelException;
import com.s13g.winston.shared.ChannelType;
import com.s13g.winston.master.channel.ChannelValue;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A Winston sensor node temperature channel.
 */
public class WinstonSensorNodeChannel implements Channel {
  private final WinstonSensorNodeController mSensorNodeController;

  public WinstonSensorNodeChannel(WinstonSensorNodeController sensorNodeController) {
    mSensorNodeController = sensorNodeController;
  }

  @Override
  public String getChannelId() {
    return mSensorNodeController.getNodeAddress();
  }

  @Override
  public ChannelType getType() {
    return ChannelType.WINSTON_SENSORBOX;
  }

  @Override
  public List<ChannelValue> getValues() {
    List<ChannelValue> channels = new LinkedList<>();
    int count = 0;
    for (Supplier<Optional<Temperature>> supplier : mSensorNodeController.getTemperatureSensors()) {
      channels.add(new TemperatureCelsiusSensorChannelValue("tempC-" + count++, supplier));
    }
    return ImmutableList.copyOf(channels);
  }

  private class TemperatureCelsiusSensorChannelValue implements ChannelValue<Float> {
    private final String mName;
    private final Supplier<Optional<Temperature>> mSupplier;

    private TemperatureCelsiusSensorChannelValue(String name, Supplier<Optional<Temperature>>
        supplier) {
      mName = name;
      mSupplier = supplier;
    }

    @Override
    public Mode getMode() {
      return Mode.READ_ONLY;
    }

    @Override
    public String getName() {
      return mName;
    }

    @Override
    public void writeRaw(String value) throws ChannelException {
      throw new ChannelException("Temperature cannot be set for sensor.");
    }

    @Override
    public void write(Float value) throws ChannelException {
      throw new ChannelException("Temperature cannot be set for sensor.");
    }

    @Override
    public Float read() throws ChannelException {
      Optional<Temperature> temperature = mSupplier.get();
      if (!temperature.isPresent()) {
        throw new ChannelException("Unable to read temperature.");
      }
      return temperature.get().get(Temperature.Unit.CELSIUS);
    }
  }
}
