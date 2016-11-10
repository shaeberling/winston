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
import com.s13g.winston.lib.core.TypeConversion;
import com.s13g.winston.lib.tv.TvController;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.ChannelException;
import com.s13g.winston.master.channel.ChannelType;
import com.s13g.winston.master.channel.ChannelValue;

import java.util.List;

/**
 * Channel for switching Samsung TVs on and off.
 */
public class SamsungTvChannel implements Channel {
  private final String mChannelId;
  private final TvController mTvController;

  public SamsungTvChannel(String channelId, TvController tvController) {
    mChannelId = channelId;
    mTvController = tvController;
  }

  @Override
  public String getChannelId() {
    return mChannelId;
  }

  @Override
  public ChannelType getType() {
    return ChannelType.SAMSUNG_TV;
  }

  @Override
  public List<ChannelValue> getValues() {
    ChannelValue powerValue = new SamsungPowerChannelValue();
    return ImmutableList.of(powerValue);
  }

  private class SamsungPowerChannelValue implements ChannelValue<Boolean> {

    @Override
    public Mode getMode() {
      return Mode.WRITE_ONLY;
    }

    @Override
    public String getName() {
      return "power";
    }

    @Override
    public void writeRaw(String value) throws ChannelException {
      try {
        write(TypeConversion.stringToBoolean(value));
      } catch (TypeConversion.IllegalFormatException e) {
        throw new ChannelException(e.getMessage());
      }
    }

    @Override
    public void write(Boolean value) throws ChannelException {
      if (value) {
        throw new ChannelException("Cannot power on the TV");
      }
      mTvController.switchOff();
    }

    @Override
    public Boolean read() throws ChannelException {
      throw new ChannelException("Reading power status not supported yet.");
    }
  }
}
