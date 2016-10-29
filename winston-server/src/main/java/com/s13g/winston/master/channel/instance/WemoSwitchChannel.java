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
import com.s13g.winston.lib.wemo.WemoSwitch;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.ChannelException;
import com.s13g.winston.master.channel.ChannelValue;

import java.util.List;
import java.util.Optional;

/**
 * A channel for a Wemo switch.
 */
public class WemoSwitchChannel implements Channel {
  private final String mChannelId;
  private final WemoSwitch mSwitch;

  public WemoSwitchChannel(String channelId, WemoSwitch aSwitch) {
    mChannelId = channelId;
    mSwitch = aSwitch;
  }

  @Override
  public String getChannelId() {
    return mChannelId;
  }

  @Override
  public List<ChannelValue> getValues() {
    ChannelValue value = new WemoSwitchStateChannelValue();
    return ImmutableList.of(value);
  }

  private class WemoSwitchStateChannelValue implements ChannelValue<Boolean> {

    @Override
    public Mode getType() {
      return Mode.READ_WRITE;
    }

    @Override
    public void write(Boolean value) throws ChannelException {
      mSwitch.setSwitch(value);
    }

    @Override
    public Boolean read() throws ChannelException {
      Optional<Boolean> isOn = mSwitch.isOn();
      if (!isOn.isPresent()) {
        throw new ChannelException("Unable to get Wemo Switch state");
      }
      return isOn.get();
    }
  }
}
