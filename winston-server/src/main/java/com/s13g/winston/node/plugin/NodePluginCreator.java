/*
 * Copyright 2015 The Winston Authors
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


package com.s13g.winston.node.plugin;

import com.pi4j.io.gpio.GpioController;
import com.s13g.winston.lib.led.LedController;
import com.s13g.winston.lib.led.LedControllerImpl;
import com.s13g.winston.lib.plugin.NodeController;
import com.s13g.winston.lib.plugin.NodePluginType;
import com.s13g.winston.lib.plugin.ReedToLedPlugin;
import com.s13g.winston.lib.reed.ReedController;
import com.s13g.winston.lib.reed.ReedControllerImpl;
import com.s13g.winston.lib.relay.RelayController;
import com.s13g.winston.lib.relay.RelayControllerImpl;
import com.s13g.winston.node.handler.Handler;
import com.s13g.winston.node.handler.LedHandler;
import com.s13g.winston.node.handler.ReedHandler;
import com.s13g.winston.node.handler.RelayHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates NodePlugins.
 */
public class NodePluginCreator {
  private static final Logger LOG = LogManager.getLogger(NodePluginCreator.class);
  private final GpioController mGpioController;

  /**
   * We keep a cache of active controllers so that controllers that need others as parameters
   * can access them.
   */
  private final Map<NodePluginType, NodePlugin> mActiveControllers = new HashMap<>();

  /**
   * Constructor for node controller creator.
   *
   * @param gpioController the GPIO controller is passed into the controllers that need access to
   *                       the GPIO pins.
   */
  public NodePluginCreator(GpioController gpioController) {
    mGpioController = gpioController;
  }

  /**
   * Create a node controller.
   *
   * @param name    the name of the node controller, must be a valid NodePluginsType.
   * @param mapping mapping to be used for this controller. Semantics depend on the given
   *                controller.
   * @return The controller.
   * @throws RuntimeException if the controller could not be instantiated.
   */
  public NodePlugin create(String name, int[] mapping) {
    NodePlugin plugin = createInternal(name, mapping);
    mActiveControllers.put(plugin.type, plugin);
    return plugin;
  }

  /** Actually creating the controller. */
  private NodePlugin createInternal(String name, int[] mapping) {
    NodePluginType pluginType = null;
    try {
      pluginType = NodePluginType.valueOf(name.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      LOG.error("Illegal plugin name: " + name);
      throw new RuntimeException("No controller for name: " + name);
    }

    // Add new controllers here. If a controller/plugin requires other controllers as its
    // parameters, they need to be later in the configuration file so that the dependencies are
    // created first. We do not (yet?) have dynamic dependency graph resolution ;)
    NodeController controller;
    Handler handler;

    switch (pluginType) {
      case LED:
        controller = new LedControllerImpl(mapping, mGpioController);
        handler = new LedHandler((LedController) controller);
        break;
      case REED:
        controller = new ReedControllerImpl(mapping, mGpioController);
        handler = new ReedHandler((ReedController) controller);
        break;
      case RELAY:
        controller = new RelayControllerImpl(mapping, mGpioController);
        handler = new RelayHandler((RelayController) controller);
        break;
      case _REEDTOLED:
        controller = new ReedToLedPlugin(mapping, (ReedController) mActiveControllers.get
            (NodePluginType.REED).controller, (LedController) mActiveControllers.get
            (NodePluginType.LED).controller);
        handler = null;  // This plugins does not have a handler.
        break;
      default:
        throw new RuntimeException("No controller defined for valid name: " + name);
    }
    return new NodePlugin(pluginType, controller, handler);
  }
}
