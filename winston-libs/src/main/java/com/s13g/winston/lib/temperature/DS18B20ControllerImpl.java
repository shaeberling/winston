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

package com.s13g.winston.lib.temperature;

import com.s13g.winston.lib.core.file.ReadableFile;
import com.s13g.winston.lib.plugin.NodePluginType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Communicates with the DS18B20 temp sensor via 1-wire.
 * <p>
 * This is a simplified version that works with this sensor. 1-wire support has recently been added
 * to PI4J's development branch. Once it becomes stable, we should use it instead. See
 * https://github.com/Pi4J/pi4j/issues/32
 * <p>
 * To enable 1wire on the Raspberry PI, see https://goo.gl/zc0iKt.
 */
public class DS18B20ControllerImpl implements TemperatureSensorController {
  private static final Logger LOG = LogManager.getLogger(DS18B20ControllerImpl.class);

  private static final String READ_VALUE_PATH = "/sys/bus/w1/devices/%s/w1_slave";

  /** The path to read the device. */
  private final ReadableFile mDevicePath;

  /**
   * getLastTemperature good known temperature reading.
   * <p>
   * TODO: Add timestamp so we know when it becomes too old.
   */
  private Temperature mLastKnownGoodTemperature = new Temperature(-42, Temperature.Unit.CELSIUS);

  /**
   * Initialize the temperature sensor for the given device.
   *
   * @param deviceName e.g. '28-000005abd27d'. Find it by checking '/sys/bus/w1/devices'.
   */
  public DS18B20ControllerImpl(String deviceName, ReadableFile.Creator creator) {
    mDevicePath = creator.create(Paths.get(String.format(READ_VALUE_PATH, deviceName)));
  }

  private int readValue() throws IOException {
    if (!mDevicePath.exists()) {
      throw new IOException("Sensor file does not exist: " + mDevicePath);
    }
    if (!mDevicePath.isReadable()) {
      throw new IOException("Sensor file is not readable: " + mDevicePath);
    }

    String strValue;
    try {
      strValue = mDevicePath.readAsString();
    } catch (IOException ex) {
      throw new IOException("Cannot read sensor file: " + mDevicePath, ex);
    }

    // Line look like this, for example:
    // 93 01 4b 46 7f ff 0d 10 32 : crc=32 YES
    // 93 01 4b 46 7f ff 0d 10 32 t=25187
    // First line tells us whether the reading was successful.
    // Second line contains the value at the end.
    String[] lines = strValue.split("\\r?\\n");

    // First, check if the read out was successful.
    if (!lines[0].endsWith("YES")) {
      throw new IOException("Native read-out was not successful, ignoring value.");
    }

    // Find the milli-celsius value.
    int startMarker = lines[1].lastIndexOf("t=");
    if (startMarker == -1) {
      throw new IOException("Could not find valid temperature value.\n" + lines[1]);
    }

    // Parse the value into an integer and return.
    try {
      return Integer.parseInt(lines[1].substring(startMarker + 2, lines[1].length()));
    } catch (NumberFormatException ex) {
      throw new IOException("Could not parse temperature value.\n" + lines[1]);
    }
  }

  @Override
  public Temperature getTemperature() {
    try {
      mLastKnownGoodTemperature = new Temperature(readValue() / 1000f, Temperature.Unit
          .CELSIUS);
    } catch (IOException ex) {
      LOG.warn("Could not read temperature", ex);
    }
    return mLastKnownGoodTemperature;
  }

  @Override
  public NodePluginType getType() {
    return NodePluginType.DS18B20_TEMP;
  }
}