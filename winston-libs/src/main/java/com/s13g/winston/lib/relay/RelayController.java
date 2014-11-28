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

package com.s13g.winston.lib.relay;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

/**
 * The relay controller interface which can be implemented on top of the actual
 * GPIOs using Pi4J or on top of a proxy.
 */
public interface RelayController {
  /** Simple mapping from number to GPIO Pin. */
  static Pin[] GPIO_PIN = new Pin[] { RaspiPin.GPIO_00, RaspiPin.GPIO_01, RaspiPin.GPIO_02,
      RaspiPin.GPIO_03, RaspiPin.GPIO_04, RaspiPin.GPIO_05, RaspiPin.GPIO_06, RaspiPin.GPIO_07,
      RaspiPin.GPIO_08, RaspiPin.GPIO_09, RaspiPin.GPIO_10, RaspiPin.GPIO_11, RaspiPin.GPIO_12,
      RaspiPin.GPIO_13, RaspiPin.GPIO_14, RaspiPin.GPIO_15, RaspiPin.GPIO_16, RaspiPin.GPIO_17,
      RaspiPin.GPIO_18, RaspiPin.GPIO_19, RaspiPin.GPIO_20, RaspiPin.GPIO_21, RaspiPin.GPIO_22,
      RaspiPin.GPIO_23, RaspiPin.GPIO_24, RaspiPin.GPIO_25, RaspiPin.GPIO_26, RaspiPin.GPIO_27,
      RaspiPin.GPIO_28, RaspiPin.GPIO_29 };

  /**
   * Switches the relay with the given number.
   *
   * @param num
   *          the relay number, starting with 0.
   * @param on
   *          Whether to switch it on, otherwise off.
   */
  public void switchRelay(int num, boolean on);

  /**
   * Performs a standard click (on/off) with the default delay.
   * <p>
   * If the relay is currently on, no click will be performed. The relay has to
   * be off when this method is called.
   *
   * @param num
   *          the relay to click.
   */
  public void clickRelay(int num);
}
