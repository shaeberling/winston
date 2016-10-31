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
import com.s13g.winston.common.TypeConversion;
import com.s13g.winston.lib.winston.WinstonPowerNodeController;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.ChannelException;
import com.s13g.winston.master.channel.ChannelValue;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * A channel for a winston power node.
 */
public class WinstonPowerNodeChannel implements Channel {
  private final WinstonPowerNodeController mPowerNodeController;

  public WinstonPowerNodeChannel(WinstonPowerNodeController powerNodeController) {
    mPowerNodeController = powerNodeController;
  }

  @Override
  public String getChannelId() {
    return mPowerNodeController.getNodeAddress();
  }

  @Override
  public List<ChannelValue> getValues() {
    List<ChannelValue> channels = new LinkedList<>();
    int count = 0;
    for (Function<Boolean, Boolean> powerSwticher : mPowerNodeController.getSwitchChanger()) {
      channels.add(new PowerSwitchChannelValue("outlet-" + count++, powerSwticher));
    }
    return ImmutableList.copyOf(channels);
  }

  private class PowerSwitchChannelValue implements ChannelValue<Boolean> {
    private final String mName;
    private final Function<Boolean, Boolean> mSwitcher;

    private PowerSwitchChannelValue(String name, Function<Boolean, Boolean> switcher) {
      mName = name;
      mSwitcher = switcher;
    }

    @Override
    public Mode getType() {
      // TODO: Need to implment read-functionality on the node first.
      return Mode.WRITE_ONLY;
    }

    @Override
    public String getName() {
      return mName;
    }

    @Override
    public void writeRaw(String value) throws ChannelException {
      try {
        write(TypeConversion.stringToBoolean(value));
      } catch (TypeConversion.IllegalFormatException e) {
        throw new ChannelException("Cannot switch, invalid input", e);
      }
    }

    @Override
    public void write(Boolean on) throws ChannelException {
      if (!mSwitcher.apply(on)) {
        throw new ChannelException("Unable to switch power channel value.");
      }
    }

    @Override
    public Boolean read() throws ChannelException {
      throw new ChannelException("Read not supported yet.");
    }
  }
}
