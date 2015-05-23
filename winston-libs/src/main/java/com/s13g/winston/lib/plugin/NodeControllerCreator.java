/*
 * Copyright 2015 Sascha Haeberling
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

import com.pi4j.io.gpio.GpioController;
import com.s13g.winston.lib.led.LedController;
import com.s13g.winston.lib.led.LedControllerImpl;
import com.s13g.winston.lib.reed.ReedController;
import com.s13g.winston.lib.reed.ReedControllerImpl;
import com.s13g.winston.lib.relay.RelayControllerImpl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates node controllers.
 */
public class NodeControllerCreator {
  private static final Logger LOG = LogManager.getLogger(NodeControllerCreator.class);
  private final GpioController mGpioController;
  private final Map<NodePluginType, NodeController> mActiveControllers = new HashMap<>();

  public NodeControllerCreator(GpioController gpioController) {
    mGpioController = gpioController;
  }

  public NodeController create(String name, int[] mapping) {
    NodeController controller = createInternal(name, mapping);
    mActiveControllers.put(controller.getType(), controller);
    return controller;
  }

  private NodeController createInternal(String name, int[] mapping) {
    NodePluginType pluginType = null;
    try {
      pluginType = NodePluginType.valueOf(name.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      LOG.error("Illegal plugin name: " + name);
      throw new RuntimeException("No controller for name: " + name);
    }

    switch (pluginType) {
      case LED:
        return new LedControllerImpl(mapping, mGpioController);
      case REED:
        return new ReedControllerImpl(mapping, mGpioController);
      case RELAY:
        return new RelayControllerImpl(mapping, mGpioController);
      case _REEDTOLED:
        return new ReedToLedPlugin(mapping, (ReedController) mActiveControllers.get
            (NodePluginType.REED), (LedController) mActiveControllers.get(NodePluginType.LED));
      default:
        throw new RuntimeException("No controller defined valid name: " + name);
    }
  }
}
