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
import com.s13g.winston.shared.data.Temperature;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Communicates with a Winston sensor node.
 */
public class WinstonSensorNodeController {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private final String mNodeAddress;
  private final List<Supplier<Optional<Temperature>>> mTemperatureSensors;

  WinstonSensorNodeController(String nodeAddress) {
    mNodeAddress = nodeAddress;
    mTemperatureSensors = new LinkedList<>();
  }

  public void addTemperatureSensor(String path) {
    mTemperatureSensors.add(forTemperatureSensor(path));
  }

  public List<Supplier<Optional<Temperature>>> getTemperatureSensors() {
    return ImmutableList.copyOf(mTemperatureSensors);
  }

  public String getNodeAddress() {
    return mNodeAddress;
  }

  private Supplier<Optional<Temperature>> forTemperatureSensor(String path) {
    // TODO: Port should not be hardcoded.
    final String addressFmt = "http://%s:1984/io/%s";
    final String address = String.format(addressFmt, mNodeAddress, path);
    return () -> {
      String tempStr = "";
      try {
        tempStr = HttpUtil.requestUrl(address);
        return Optional.of(Temperature.parse(tempStr));
      } catch (IllegalArgumentException e) {
        log.atWarning().withCause(e).log("Request to '%s'resulted in illegal " +
            "temperature value request temperature: '%s'", path, tempStr);
        return Optional.empty();
      } catch (IOException e) {
        log.atWarning().withCause(e).log("Cannot request temperature from '%s'.", path);
        return Optional.empty();
      }
    };
  }
}
