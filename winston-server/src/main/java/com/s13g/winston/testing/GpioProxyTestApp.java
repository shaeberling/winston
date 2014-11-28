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
import com.s13g.winston.lib.core.SingletonProvider;
import com.s13g.winston.lib.relay.RelayController;
import com.s13g.winston.lib.relay.RelayControllerFactory;

/**
 * A stand-alone test application that is performing a few random GPIO tests
 * <p>
 * This is intended to be used either to be run on a Raspberry PI directly or on
 * a computer using the Pi4J Proxy implementation.
 */
public class GpioProxyTestApp {
  private static Logger LOG = LogManager.getLogger(GpioProxyTestApp.class);
  private static final boolean USE_GPIO_PROXY = true;

  public static void main(String[] args) throws InterruptedException {
    LOG.info("Starting up");

    final SingletonProvider<GpioController> gpioController = SingletonProvider
        .from(() -> GpioFactory.getInstance());
    final RelayController relayController = RelayControllerFactory.create(new int[] { 1, 2, 3, 4 },
        gpioController, "192.168.1.120:1984");

    allRelaysOff(relayController);
    for (int relay = 0; relay <= 3; ++relay) {
      relayController.clickRelay(relay);
      Thread.sleep(50);
    }

    allRelaysOff(relayController);
    for (int loops = 0; loops < 3; ++loops) {
      for (int num = 0; num < 16; ++num) {
        for (int x = 1, i = 0; x <= 8; x = (x << 1), i++) {
          relayController.switchRelay(i, ((num & x) > 0));
        }
        Thread.sleep(300);
      }
    }
    Thread.sleep(2000);
    if (gpioController.isCached()) {
      LOG.info("Shutting down gpioController.");
      gpioController.provide().shutdown();
    }
    LOG.info("And we're done here.");
  }

  private static void allRelaysOff(RelayController relayController) {
    for (int relay = 0; relay <= 3; ++relay) {
      relayController.switchRelay(relay, false);
    }
  }
}
