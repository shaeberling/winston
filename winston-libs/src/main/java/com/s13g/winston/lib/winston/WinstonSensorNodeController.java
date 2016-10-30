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

import com.s13g.winston.lib.core.net.HttpUtil;
import com.s13g.winston.lib.temperature.Temperature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Communicates with a Winston sensor node.
 */
public class WinstonSensorNodeController {
  private static final Logger LOG = LogManager.getLogger(WinstonSensorNodeController.class);

  private final String mNodeAddress;

  WinstonSensorNodeController(String nodeAddress) {
    mNodeAddress = nodeAddress;
  }

  public Supplier<Optional<Temperature>> forTemperatureSensor(String path) {
    final String addressFmt = "http://%s:1984/io/%s";
    final String address = String.format(addressFmt, mNodeAddress, path);
    return () -> {
      String tempStr = "";
      try {
        tempStr = HttpUtil.requestUrl(address);
        return Optional.of(Temperature.parse(tempStr));
      } catch (IllegalArgumentException e) {
        LOG.error("Request to '" + path + "'resulted in illegal temperature value request " +
            "temperature: '" + tempStr + "': ", e);
        return Optional.empty();
      } catch (IOException e) {
        LOG.error("Cannot request temperature from '" + path + "'.", e);
        return Optional.empty();
      }
    };
  }
}
