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

package com.s13g.winston.lib.winston;

import com.google.common.collect.ImmutableList;
import com.s13g.winston.lib.core.net.HttpUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Controller for interfacing with a winston power node, such as the power box.
 */
public class WinstonPowerNodeController {
  private static final Logger LOG = LogManager.getLogger(WinstonPowerNodeController.class);

  private final String mNodeAddress;
  private final List<Function<Boolean, Boolean>> mSwitchPower;
  // TODO: Need to make the node support returning the state so we can add reading from it.

  WinstonPowerNodeController(String nodeAddress) {
    mNodeAddress = nodeAddress;
    mSwitchPower = new LinkedList<>();
  }

  public void addSwitch(String path) {
    mSwitchPower.add(forSwitchChange(path));
  }

  public List<Function<Boolean, Boolean>> getSwitchChanger() {
    return ImmutableList.copyOf(mSwitchPower);
  }

  public String getNodeAddress() {
    return mNodeAddress;
  }

  private Function<Boolean, Boolean> forSwitchChange(String path) {
    final String addressFmt = "http://%s:1984/io/%s/%s";
    return (on) -> {
      final String address = String.format(addressFmt, mNodeAddress, path, on ? "1" : "0");
      try {
        String response = HttpUtil.requestUrl(address);
        return "OK".equals(response);
      } catch (IOException e) {
        LOG.error("Cannot perform switch action '" + address + "'.");
      }
      return false;
    };
  }
}
