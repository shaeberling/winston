/*
 * Copyright 2014 Sascha Haeberling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.s13g.winston.lib.led;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.s13g.winston.lib.core.Pins;
import com.s13g.winston.lib.plugin.NodePluginType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * A controller that can be used to switch LED by controlling the HIGH/LOW state
 * of the given GPIO pins.
 */
public class LedControllerImpl implements LedController {
  private static final Logger LOG = LogManager.getLogger(LedControllerImpl.class);
  private final GpioPinDigitalOutput[] mPins;

  public LedControllerImpl(int[] mapping, GpioController gpioController) {
    LOG.info("Initializing with mapping: " + Arrays.toString(mapping));
    mPins = initializePins(mapping, gpioController);
  }

  private static GpioPinDigitalOutput[] initializePins(int mapping[],
                                                       GpioController gpioController) {
    final GpioPinDigitalOutput[] pins = new GpioPinDigitalOutput[mapping.length];
    for (int i = 0; i < mapping.length; ++i) {
      pins[i] = gpioController.provisionDigitalOutputPin(Pins.GPIO_PIN[mapping[i]], PinState.LOW);
      pins[i].setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
    }
    return pins;
  }

  @Override
  public void switchLed(int num, boolean on) {
    if (num < 0 || num >= mPins.length) {
      LOG.warn("Invalid LED number: " + num);
      return;
    }
    mPins[num].setState(on ? PinState.HIGH : PinState.LOW);
  }

  @Override
  public NodePluginType getType() {
    return NodePluginType.LED;
  }
}
