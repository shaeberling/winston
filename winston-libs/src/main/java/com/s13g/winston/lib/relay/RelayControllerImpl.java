/*
 * Copyright 2015 The Winston Authors
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

package com.s13g.winston.lib.relay;

import com.google.common.flogger.FluentLogger;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.s13g.winston.lib.core.Pins;
import com.s13g.winston.lib.plugin.NodePluginType;

import java.util.Arrays;
import java.util.HashMap;

/**
 * A RelayController that uses Pi4J to access the GPIO pins directly.
 */
public class RelayControllerImpl implements RelayController {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private static final int DEFAULT_CLICK_DELAY_MILLIS = 500;

  private final int mClickDelay;

  /**
   * Maps relay number to GPIO number.
   */
  private final int[] mMapping;
  private final GpioController mGpioController;

  private final HashMap<Integer, GpioPinDigitalOutput> mActivePins = new HashMap<>();

  public RelayControllerImpl(int[] mapping, GpioController gpioController) {
    log.atInfo().log("Initializing with mapping: " + Arrays.toString(mapping));
    mMapping = mapping;
    mGpioController = gpioController;
    mClickDelay = DEFAULT_CLICK_DELAY_MILLIS;
  }

  RelayControllerImpl(int[] mapping, GpioController gpioController, int clickDelay) {
    log.atInfo().log("Initializing with mapping: " + Arrays.toString(mapping));
    mMapping = mapping;
    mGpioController = gpioController;
    mClickDelay = clickDelay;
  }

  @Override
  public synchronized void switchRelay(int num, boolean on) {
    if (num < 0 || num >= mMapping.length) {
      log.atWarning().log("Invalid relay number: %d", num);
      return;
    }

    log.atInfo().log("Switching relay" + num + " on? " + on);
    GpioPinDigitalOutput gpio = mActivePins.get(num);
    if (gpio != null) {
      // If we already initialize the pin, simply switch the state.
      gpio.setState(on ? PinState.LOW : PinState.HIGH);
      return;
    }

    // A relay is 'ON' when its pin is set to LOW. However, there is an issue if
    // we call provisionDigitalOutputPin with state HIGH to have the pin off as
    // it will very briefly switch it to LOW. This is not acceptable, since e.g.
    // we might accidentally open up a garage door when initializing the pins.
    // This is why we don't provision the pin here until we need it.

    if (!on) {
      // If a pin has not been provisioned yet and is supposed to be switched
      // 'off', don't do anything.
      return;
    }

    // Only if a pin has not been provisioned yet and is to be switched on,
    // provision it to the relay state 'ON' which is 'LOW'.
    final GpioPinDigitalOutput pin = mGpioController.provisionDigitalOutputPin(
        Pins.GPIO_PIN[mMapping[num]], PinState.LOW);

    // Important: When the program is exited the pin needs to go into 'HIGH'
    // state so that the relay stays off. We don't want garage doors to open
    // when the app exits.
    pin.setShutdownOptions(true, PinState.HIGH, PinPullResistance.OFF);
    mActivePins.put(num, pin);
  }

  @Override
  public synchronized void clickRelay(int num) {
    if (isRelayOn(num)) {
      log.atWarning().log("Cannot click relay %d since it appears to be on.", num);
      return;
    }

    try {
      switchRelay(num, true);
      Thread.sleep(mClickDelay);
      switchRelay(num, false);
    } catch (final InterruptedException ex) {
      log.atWarning().log("Interrupted during click event.");
    }
  }

  public synchronized boolean isRelayOn(int num) {
    GpioPinDigitalOutput gpio = mActivePins.get(num);
    return gpio != null && gpio.isLow();
  }

  @Override
  public NodePluginType getType() {
    return NodePluginType.RELAY;
  }
}
