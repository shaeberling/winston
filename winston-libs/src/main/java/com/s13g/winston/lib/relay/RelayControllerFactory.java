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

import com.pi4j.io.gpio.GpioController;
import com.s13g.winston.lib.core.Provider;

public class RelayControllerFactory {

  /**
   * Creates a relay controller.
   *
   * @param mapping
   *          A list of GPIO pins, once for each relay used.
   * @param gpioController
   *          the gpio controller to use if we're not using a proxy, wrapped in
   *          a provider. If proxyUrl is given, no gpioController is needed.
   * @param proxyUrl
   *          Can be null. If given, a proxy implementation will be used,
   *          otherwise the real implementation that uses GPIO directly.
   * @return A relay controller.
   */
  public static RelayController create(int[] mapping, Provider<GpioController> gpioController,
      String proxyUrl) {
    return proxyUrl != null ? new RelayControllerProxyClientImpl(proxyUrl)
        : new RelayControllerImpl(mapping, gpioController.provide());
  }
}