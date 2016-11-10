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
import com.s13g.winston.lib.winston.WinstonGarageNodeController;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.ChannelException;
import com.s13g.winston.master.channel.ChannelType;
import com.s13g.winston.master.channel.ChannelValue;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Controller for interfacing with a Winston garage node.
 */
public class WinstonGarageNodeChannel implements Channel {
  private final List<String> mDoorNames;
  private final WinstonGarageNodeController mGarageNodeController;

  public WinstonGarageNodeChannel(List<String> doorNames,
                                  WinstonGarageNodeController garageNodeController) {
    mDoorNames = doorNames;
    mGarageNodeController = garageNodeController;
  }

  @Override
  public String getChannelId() {
    return mGarageNodeController.getNodeAddress();
  }

  @Override
  public ChannelType getType() {
    return ChannelType.WINSTON_GARAGE;
  }

  @Override
  public List<ChannelValue> getValues() {
    List<ChannelValue> channels = new LinkedList<>();
    List<Supplier<Optional<Boolean>>> closedStates = mGarageNodeController.getClosedStates();
    List<Supplier<Boolean>> clickers = mGarageNodeController.getClickers();
    // TODO: Might need to make the door names more machine readable/valid.
    for (int i = 0; i < mDoorNames.size(); ++i) {
      channels.add(new DoorOpenSensorChannelValue(
          "closedState-" + mDoorNames.get(i),
          closedStates.get(i)));
      channels.add(new DoorClickerChannelValue(
          "clicker-" + mDoorNames.get(i),
          clickers.get(i)));
    }
    return ImmutableList.copyOf(channels);
  }

  private class DoorClickerChannelValue implements ChannelValue<Boolean> {
    private final String mName;
    private final Supplier<Boolean> mClicker;

    private DoorClickerChannelValue(String name, Supplier<Boolean> clicker) {
      mName = name;
      mClicker = clicker;
    }

    @Override
    public Mode getMode() {
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
        throw new ChannelException("Cannot activate clicker, invalid input", e);
      }
    }

    @Override
    public void write(Boolean value) throws ChannelException {
      if (!value) {
        return;
      }
      if (!mClicker.get()) {
        throw new ChannelException("Error during garage door clicker activation.");
      }
    }

    @Override
    public Boolean read() throws ChannelException {
      throw new ChannelException("Door clicker cannot be read from.");
    }
  }

  private class DoorOpenSensorChannelValue implements ChannelValue<Boolean> {
    private final String mName;
    private final Supplier<Optional<Boolean>> mSupplier;

    private DoorOpenSensorChannelValue(String name, Supplier<Optional<Boolean>> supplier) {
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
      throw new ChannelException("Door status sensor cannot be written to.");
    }

    @Override
    public void write(Boolean value) throws ChannelException {
      throw new ChannelException("Door status sensor cannot be written to.");
    }

    @Override
    public Boolean read() throws ChannelException {
      Optional<Boolean> closedState = mSupplier.get();
      if (!closedState.isPresent()) {
        throw new ChannelException("Cannot determine closed status of " + mName);
      }
      return closedState.get();
    }
  }
}
