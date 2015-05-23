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

import com.s13g.winston.lib.plugin.NodeController;

/**
 * The relay controller interface which can be implemented on top of the actual
 * GPIOs using Pi4J or on top of a proxy.
 */
public interface RelayController extends NodeController {

  /**
   * Switches the relay with the given number.
   *
   * @param num the relay number, starting with 0.
   * @param on  Whether to switch it on, otherwise off.
   */
  void switchRelay(int num, boolean on);

  /**
   * Performs a standard click (on/off) with the default delay.
   * <p>
   * If the relay is currently on, no click will be performed. The relay has to
   * be off when this method is called.
   *
   * @param num the relay to click.
   */
  void clickRelay(int num);
}
