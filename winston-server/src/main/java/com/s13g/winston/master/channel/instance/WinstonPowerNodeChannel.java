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
import com.s13g.winston.lib.winston.WinstonPowerNodeController;
import com.s13g.winston.lib.winston.WinstonPowerNodeController.SwitchActions;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.ChannelException;
import com.s13g.winston.shared.ChannelType;
import com.s13g.winston.master.channel.ChannelValue;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

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
  public ChannelType getType() {
    return ChannelType.WINSTON_POWERBOX;
  }

  @Override
  public List<ChannelValue> getValues() {
    List<ChannelValue> channels = new LinkedList<>();
    int count = 0;
    for (SwitchActions switchAction : mPowerNodeController.getSwitchActions()) {
      channels.add(new PowerSwitchChannelValue(
          "outlet-" + count++, switchAction.getSwitchPower(), switchAction.getStatusReader()));
    }
    return ImmutableList.copyOf(channels);
  }

  private class PowerSwitchChannelValue implements ChannelValue<Boolean> {
    private final String mName;
    private final Function<Boolean, Boolean> mSwitcher;
    private final Supplier<Optional<Boolean>> mStatusReader;

    private PowerSwitchChannelValue(String name,
                                    Function<Boolean, Boolean> switcher,
                                    Supplier<Optional<Boolean>> statusReader) {
      mName = name;
      mSwitcher = switcher;
      mStatusReader = statusReader;
    }

    @Override
    public Mode getMode() {
      return Mode.READ_WRITE;
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
      Optional<Boolean> statusOpt = mStatusReader.get();
      if (!statusOpt.isPresent()) {
        throw new ChannelException("Cannot read switch status for '" + mName + "'.");
      }
      return statusOpt.get();
    }
  }
}
