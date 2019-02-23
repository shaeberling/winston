/*
 * Copyright 2019 The Winston Authors
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

package com.s13g.winston.lib.clitests;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.flogger.FluentLogger;
import com.s13g.winston.lib.temperature.HTU21DControllerImpl;
import com.s13g.winston.shared.data.Temperature;

import java.util.Optional;

/** CLI to test the HTU21D temperature sensor. */
public final class HTU21DTestCli {
  /** Options for this CLI. */
  static class Options {
    @Parameter(names = "-bus", description = "I2C bus of the sensor")
    Integer bus = 1;

    @Parameter(names = "-address", description = "I2C address of the sensor",
        validateWith = ValidNonNegativeInteger.class, converter = IntDecodeConverter.class)
    int address = 0x40;
  }

  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private void run(int bus, int address) {
    log.atInfo().log("Looking for sensor on bus %d and address 0x%s.", bus,
        Integer.toHexString(address));
    Optional<HTU21DControllerImpl> controllerOpt = HTU21DControllerImpl.create(bus, address);

    if (!controllerOpt.isPresent()) {
      log.atSevere().log("Cannot create the controller");
      System.exit(-1);
    }

    HTU21DControllerImpl controller = controllerOpt.get();
    Optional<Temperature> tempOpt = controller.getTemperature();
    if (!tempOpt.isPresent()) {
      log.atWarning().log("Cannot read temperature");
    } else {
      log.atInfo().log("Temperature: %s", tempOpt.get().getRounded(Temperature.Unit.CELSIUS));
    }
    Optional<Integer> humOpt = controller.getHumidityPercent();
    if (!humOpt.isPresent()) {
      log.atWarning().log("Cannot read humidity");
    } else {
      log.atInfo().log("Humidity is %d%%", humOpt.get());
    }
  }

  public static void main(String[] args) {
    log.atInfo().log("HTU21D test CLI starting up ...");
    Options options = new Options();
    JCommander.newBuilder()
        .addObject(options)
        .build()
        .parse(args);
    new HTU21DTestCli().run(options.bus, options.address);
  }

  /** Decode string to integer. */
  public static class IntDecodeConverter implements IStringConverter<Integer> {
    @Override
    public Integer convert(String value) {
      return Integer.decode(value);
    }
  }

  /** Ensure that the string can be decoded and does not result in a negative integer. */
  public static class ValidNonNegativeInteger implements IParameterValidator {
    public void validate(String name, String value) throws ParameterException {
      int n;
      try {
        n = Integer.decode(value);
      } catch (NumberFormatException ex) {
        n = -1;
      }
      if (n < 0) {
        throw new ParameterException(String.format(
            "Cannot parse parameter '%s' which is '%s' as a number.", name, value));
      }
    }
  }
}
