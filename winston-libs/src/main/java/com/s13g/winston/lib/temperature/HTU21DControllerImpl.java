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

import com.google.common.flogger.FluentLogger;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.s13g.winston.lib.plugin.NodeController;
import com.s13g.winston.lib.plugin.NodePluginType;
import com.s13g.winston.shared.data.Temperature;

import java.io.IOException;
import java.util.Optional;

/**
 * Reads temperature and humidity from the HTU21D I2C device. See the following URL for specs:
 * https://cdn.sparkfun.com/assets/6/a/8/e/f/525778d4757b7f50398b4567.pdf
 */
public class HTU21DControllerImpl implements TemperatureSensorController, AutoCloseable {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  /** Trigger Temperature Measurement */
  private final static int HTU21DF_READTEMP = 0xE3;
  /** Trigger Humidity Measurement */
  private final static int HTU21DF_READHUM = 0xE5;

  /** Trigger Temperature Measurement. No Hold master. */
  private final static int HTU21DF_READTEMP_NH = 0xF3;
  /** Trigger Humidity Measurement. No Hold master. */
  private final static int HTU21DF_READHUMI_NH = 0xF5;
  /** Write user register. */
  private final static int HTU21DF_WRITEREG = 0xE6;
  /** Read user register. */
  private final static int HTU21DF_READREG = 0xE7;
  /** Soft Reset. */
  private final static int HTU21DF_RESET = 0xFE;

  /** The bus on which the device is connected to. */
  private final I2CBus mBus;
  /** The I2C device instance of the device. */
  private final I2CDevice mDevice;

  public static Optional<HTU21DControllerImpl> create(int busNum, int address) {
    try {
      I2CBus bus = I2CFactory.getInstance(busNum);
      log.atInfo().log("Connected to I2C bus.");

      I2CDevice device = bus.getDevice(address);
      log.atInfo().log("Connected to device");
      return Optional.of(new HTU21DControllerImpl(bus, device));
    } catch (IOException e) {
      log.atSevere().withCause(e).log("Cannot create I2C temp sensor controller.");
    } catch (I2CFactory.UnsupportedBusNumberException e) {
      log.atSevere().withCause(e).log("Bad bus numer. Cannot create I2C temp sensor controller.");
    }
    return Optional.empty();
  }

  private HTU21DControllerImpl(I2CBus bus, I2CDevice device) {
    mBus = bus;
    mDevice = device;
  }

  @Override
  public NodePluginType getType() {
    return NodePluginType.HTU21D_TEMP_HUMID;
  }

  @Override
  public Optional<Temperature> getTemperature() {
    try {
      return Optional.of(new Temperature(readTempFromDevice(), Temperature.Unit.CELSIUS));
    } catch (IOException e) {
      log.atWarning().withCause(e).log("Unable to read temperature from I2C device.");
    }
    return Optional.empty();
  }

  @Override
  public Optional<Integer> getHumidityPercent() {
    try {
      return Optional.of((int) readHumidityFromDevice());
    } catch (IOException e) {
      log.atWarning().withCause(e).log("Unable to read humidity from I2C device.", e);
    }
    return Optional.empty();
  }

  private float readTempFromDevice() throws IOException {
    softReset();
    // Tell the device to read the temperature value.
    mDevice.write((byte) (HTU21DF_READTEMP));
    waitAfterCommand();
    float raw = readRaw();
    raw *= 175.72;
    raw /= (2 << 15);
    raw -= 46.85;
    return raw;
  }

  public float readHumidityFromDevice() throws IOException {
    softReset();
    // Tell the device to read the temperature value.
    mDevice.write((byte) HTU21DF_READHUM);
    waitAfterCommand();
    float raw = readRaw();
    raw *= 125;
    raw /= (2 << 15);
    raw -= 6;
    return raw;
  }

  private int readRaw() throws IOException {
    byte[] buf = new byte[3];
    mDevice.read(buf, 0, 3);
    int msb = buf[0] & 0xFF;
    int lsb = buf[1] & 0xFF;
    // TODO: Add check for CRC to ensure value is legal.
    int crc = buf[2] & 0xFF;
    return ((msb << 8) + lsb) & 0xFFFC;
  }

  private void softReset() throws IOException {
    mDevice.write((byte) HTU21DF_RESET);
    waitAfterCommand();
  }

  @Override
  public void close() {
    try {
      mBus.close();
    } catch (IOException e) {
      log.atWarning().withCause(e).log("Error while trying to close I2C controller.", e);
    }
  }

  private void waitAfterCommand() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException ignored) {
    }
  }
}
