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

package com.s13g.winston.lib.clitests;

import com.s13g.winston.lib.wemo.WemoController;
import com.s13g.winston.lib.wemo.WemoControllerImpl;
import com.s13g.winston.lib.wemo.WemoSwitch;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * CLI to test Wemo switch library.
 */
public class WemoTestCli {
  private static final Logger LOG = Logger.getLogger("WemoTestCli");

  public static void main(String[] args) throws InterruptedException, IOException {
    LOG.info("Running...");
    WemoController controller = new WemoControllerImpl("192.168.1.174");
    WemoSwitch[] switches = controller.querySwitches();
    LOG.info("Found " + switches.length + " switches.");
    for (WemoSwitch wemoSwitch : switches) {
      LOG.info(wemoSwitch.toString());

      wemoSwitch.setSwitch(false);
      Optional<Boolean> onState = wemoSwitch.isOn();
      if (!onState.isPresent()) {
        LOG.warning("Unable to get switch state.");
      } else {
        LOG.info("Switch is " + (onState.get() ? "ON" : "OFF"));
      }
    }
  }
}
