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
import com.google.common.flogger.FluentLogger;
import com.s13g.winston.lib.core.net.HttpUtil;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Controller for interfacing with a winston power node, such as the power box.
 */
public class WinstonPowerNodeController {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private final String mNodeAddress;
  private final List<SwitchActions> mSwitches;

  WinstonPowerNodeController(String nodeAddress) {
    mNodeAddress = nodeAddress;
    mSwitches = new LinkedList<>();
  }

  public void addSwitch(String path) {
    mSwitches.add(new SwitchActions(forSwitchChange(path), forStatusReader(path)));
  }

  public List<SwitchActions> getSwitchActions() {
    return ImmutableList.copyOf(mSwitches);
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
        log.atWarning().log("Cannot perform switch action '%s'.", address);
      }
      return false;
    };
  }

  private Supplier<Optional<Boolean>> forStatusReader(String path) {
    final String addressFmt = "http://%s:1984/io/%s";
    return () -> {
      final String address = String.format(addressFmt, mNodeAddress, path);
      try {
        String response = HttpUtil.requestUrl(address);
        if ("1".equals(response) || "true".equalsIgnoreCase(response)) {
          return Optional.of(true);
        } else if ("0".equals(response) || "false".equalsIgnoreCase(response)) {
          return Optional.of(false);
        }
        log.atWarning().log("Illegal witch status response '%s'.", response);
        return Optional.empty();
      } catch (IOException e) {
        log.atWarning().log("Cannot perform switch action '%s'.", address);
      }
      return Optional.empty();
    };
  }

  public static class SwitchActions {
    private final Function<Boolean, Boolean> mSwitchPower;
    private final Supplier<Optional<Boolean>> mStatusReader;

    SwitchActions(Function<Boolean, Boolean> switchPower,
                  Supplier<Optional<Boolean>> statusReader) {
      mSwitchPower = switchPower;
      mStatusReader = statusReader;
    }

    public Function<Boolean, Boolean> getSwitchPower() {
      return mSwitchPower;
    }

    public Supplier<Optional<Boolean>> getStatusReader() {
      return mStatusReader;
    }
  }
}