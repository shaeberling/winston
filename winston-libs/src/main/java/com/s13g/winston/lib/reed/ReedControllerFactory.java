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

package com.s13g.winston.lib.reed;

import com.pi4j.io.gpio.GpioController;
import com.s13g.winston.lib.core.Provider;

/**
 * Creates a reed controller.
 */
public class ReedControllerFactory {

  /**
   * If a proxyUrl was given, this will return a proxy client implementetion of
   * the reed controller. Otherwise, a reed controller will be returned that
   * will access the GPIO pins directly.
   *
   * @param mapping
   *          the mapping of GPIO pins.
   * @param gpioController
   *          the GPIO controller.
   * @param proxyUrl
   *          if a proxy should be created, this has to point to the proxy URL
   * @return A usable reed controller.
   */
  public static ReedController create(int[] mapping, Provider<GpioController> gpioController,
      String proxyUrl) {
    if (proxyUrl != null) {
      return new ReedControllerProxyClientImpl();
    } else {
      return new ReedControllerImpl(mapping, gpioController.provide());
    }
  }
}
