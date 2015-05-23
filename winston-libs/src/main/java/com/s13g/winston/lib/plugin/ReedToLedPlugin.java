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

package com.s13g.winston.lib.plugin;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.s13g.winston.lib.led.LedController;
import com.s13g.winston.lib.reed.ReedController;

/**
 * This node plugins listens to the given reed relays and switches the given
 * LEDs on or off depending on the reed relay's status.
 */
public class ReedToLedPlugin implements NodeController, ReedController.RelayStateChangedListener {
  private static final Logger LOG = LogManager.getLogger(ReedToLedPlugin.class);

  /** Maps relay number to LED number. */
  private final HashMap<Integer, Integer> mMapping;
  private final LedController mLedController;

  public ReedToLedPlugin(int[] mapping, ReedController reedController,
                         LedController ledController) {
    mMapping = createMapping(mapping);
    mLedController = ledController;
    reedController.addListener(this);
  }

  @Override
  public void onRelayStateChanged(int relayNum, boolean closed) {
    LOG.debug("plugin: relay changed: " + relayNum + " to " + closed);
    if (!mMapping.containsKey(relayNum)) {
      // Ignore relay change since we don't listen to state changes of this one.
      return;
    }
    mLedController.switchLed(mMapping.get(relayNum), closed);
  }

  /**
   * Creates a mapping for this plugin.
   *
   * @param mapping an even number of integers, representing pairs of (reed,led)
   *                numbers.
   * @return A map that contains these pairs.
   */
  public static HashMap<Integer, Integer> createMapping(int[] mapping) {
    final HashMap<Integer, Integer> result = new HashMap<>();
    for (int i = 0; i < mapping.length; i += 2) {
      final int reedNum = mapping[i];
      final int ledNum = mapping[i + 1];
      result.put(reedNum, ledNum);
    }
    return result;
  }

  @Override
  public NodePluginType getType() {
    return NodePluginType._REEDTOLED;
  }
}
