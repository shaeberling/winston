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

package com.s13g.winston.testing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * This is a daemon that runs and exercises the GPIO API and can get controlled
 * via an HTTP RPC API.
 * <p>
 * This can be used to test and develop on a non-PI machine, while using a GPIO
 * proxy that calls this test daemon.
 * <p>
 * NOTE: Right now this is just testing some basic GPIO functions.
 */
public class GpioTestDaemon {
  private static Logger LOG = LogManager.getLogger(GpioTestDaemon.class);
  private static final int NUM_PINS = 4;
  private static final GpioPinDigitalOutput[] pins = new GpioPinDigitalOutput[4];

  public static void main(String[] args) throws InterruptedException {
    LOG.info("GPIO Test Daemon");
    final GpioController gpioController = GpioFactory.getInstance();
    initPins(gpioController);

    for (int loops = 0; loops < 3; ++loops) {
      for (int num = 0; num < 16; ++num) {
        for (int x = 1, i = 0; x <= 8; x = (x << 1), i++) {
          setRelay(i, ((num & x) > 0));
        }
        Thread.sleep(300);
      }
    }

    Thread.sleep(2000);

    LOG.info("Will switch PIN 01 to HIGH when exiting in 5 seconds.");
    gpioController.shutdown();
    LOG.info("And we're done here.");
  }

  private static void setRelay(int num, boolean on) {
    LOG.info("Setting " + num + " to " + on);
    pins[num].setState(on ? PinState.LOW : PinState.HIGH);
  }

  private static void initPins(GpioController gpioController) {
    pins[0] = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_01, PinState.HIGH);
    pins[1] = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.HIGH);
    pins[2] = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_03, PinState.HIGH);
    pins[3] = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_04, PinState.HIGH);

    for (int i = 0; i < NUM_PINS; ++i) {
      pins[i].setShutdownOptions(true, PinState.HIGH, PinPullResistance.OFF);
    }
  }
}
