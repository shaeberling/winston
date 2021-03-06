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
import com.s13g.winston.shared.data.TypeConversion;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Controller to interface with a Winston garage node.
 */
public class WinstonGarageNodeController {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private final String mNodeAddress;
  private final List<Supplier<Boolean>> mClickers;
  private final List<Supplier<Optional<Boolean>>> mClosedStates;

  WinstonGarageNodeController(String nodeAddress) {
    mNodeAddress = nodeAddress;
    mClickers = new LinkedList<>();
    mClosedStates = new LinkedList<>();
  }

  public void addClicker(String path) {
    mClickers.add(forClicker(path));
  }

  public void addClosedState(String path) {
    mClosedStates.add(forClosedState(path));
  }

  public List<Supplier<Boolean>> getClickers() {
    return ImmutableList.copyOf(mClickers);
  }

  public List<Supplier<Optional<Boolean>>> getClosedStates() {
    return ImmutableList.copyOf(mClosedStates);
  }

  public String getNodeAddress() {
    return mNodeAddress;
  }

  private Supplier<Boolean> forClicker(String path) {
    final String addressFmt = "http://%s:1984/io/%s/2";
    return () -> {
      String address = String.format(addressFmt, mNodeAddress, path);
      try {
        String response = HttpUtil.requestUrl(address);
        return "OK".equals(response);
      } catch (IOException e) {
        log.atWarning().withCause(e).log("Cannot get garage stratus '%s'", address);
      }
      return false;
    };
  }

  private Supplier<Optional<Boolean>> forClosedState(String path) {
    final String addressFmt = "http://%s:1984/io/%s";
    return () -> {
      String address = String.format(addressFmt, mNodeAddress, path);
      try {
        return Optional.of(TypeConversion.stringToBoolean(HttpUtil.requestUrl(address)));
      } catch (IOException e) {
        log.atWarning().withCause(e).log("Cannot get garage status '%s'", address);
      } catch (TypeConversion.IllegalFormatException e) {
        log.atWarning().withCause(e).log("Return value of type conversion not valid '%s'", address);
      }
      return Optional.empty();
    };
  }
}
