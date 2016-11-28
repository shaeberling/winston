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
import com.s13g.winston.lib.core.file.ReadableFile;
import com.s13g.winston.lib.led.LedController;
import com.s13g.winston.lib.led.LedControllerImpl;
import com.s13g.winston.lib.photocell.PhotoCellController;
import com.s13g.winston.lib.photocell.PhotoCellControllerImpl;
import com.s13g.winston.lib.plugin.NodeController;
import com.s13g.winston.lib.plugin.NodePluginType;
import com.s13g.winston.lib.plugin.ReedToLedPlugin;
import com.s13g.winston.lib.reed.ReedController;
import com.s13g.winston.lib.reed.ReedControllerImpl;
import com.s13g.winston.lib.relay.RelayController;
import com.s13g.winston.lib.relay.RelayControllerImpl;
import com.s13g.winston.lib.temperature.DS18B20ControllerImpl;
import com.s13g.winston.lib.temperature.HTU21DControllerImpl;
import com.s13g.winston.lib.temperature.TemperatureSensorController;
import com.s13g.winston.node.handler.Handler;
import com.s13g.winston.node.handler.LedHandler;
import com.s13g.winston.node.handler.PhotoCellHandler;
import com.s13g.winston.node.handler.ReedHandler;
import com.s13g.winston.node.handler.RelayHandler;
import com.s13g.winston.node.handler.TemperatureHandler;
import com.s13g.winston.proto.Node.NodeConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Creates NodePlugins.
 */
public class NodePluginCreator {
  private static final Logger LOG = LogManager.getLogger(NodePluginCreator.class);
  private final GpioController mGpioController;
  private ReadableFile.Creator mFileCreator;

  /**
   * We keep a cache of active controllers so that controllers that need others as parameters can
   * access them.
   */
  private final Map<NodePluginType, NodePlugin> mActiveControllers = new HashMap<>();

  /**
   * Constructor for node controller creator.
   *
   * @param gpioController the GPIO controller is passed into the controllers that need access to
   * the GPIO pins.
   */
  public NodePluginCreator(GpioController gpioController) {
    mGpioController = gpioController;
    mFileCreator = new ReadableFile.Creator();
  }

  /**
   * Create a node plugin for a GPIO based controller.
   *
   * @param gpioPlugin the GPIO plugin config.
   * @return The plugin.
   */
  public NodePlugin create(NodeConfig.GpioPlugin gpioPlugin) {
    String type = gpioPlugin.getType();
    int[] mapping = gpioPlugin.getMappingList().stream().mapToInt(i -> i).toArray();
    return createGpio(type, mapping);
  }

  /**
   * Create a node plugin for a 1-Wire based controller.
   *
   * @param oneWirePlugin the 1-Wire plugin config.
   * @return The plugin.
   */
  public NodePlugin create(NodeConfig.OneWirePlugin oneWirePlugin) {
    String type = oneWirePlugin.getType();
    String name = oneWirePlugin.getName();
    return createOneWire(type, name);
  }

  /**
   * Create a node plugin for an I2C controller.
   *
   * @param i2cPlugins the I2C plugin config.
   * @return The plugin.
   */
  public NodePlugin create(NodeConfig.I2cPlugin i2cPlugins) {
    String type = i2cPlugins.getType();
    int bus = i2cPlugins.getBus();
    int address = i2cPlugins.getAddress();
    return createI2C(type, bus, address);
  }

  /**
   * Create a node plugin for a GPIO-based controller.
   *
   * @param name the name of the node controller, must be a valid NodePluginsType.
   * @param mapping mapping to be used for this controller. Semantics depend on the given
   * controller.
   * @return The plugin.
   * @throws RuntimeException if the controller could not be instantiated.
   */
  private NodePlugin createGpio(String name, int[] mapping) {
    NodePlugin plugin = createGpioInternal(name, mapping);
    mActiveControllers.put(plugin.type, plugin);
    return plugin;
  }

  /** Actually creating the controller. */
  private NodePlugin createGpioInternal(String type, int[] mapping) {
    NodePluginType pluginType = getPluginType(type);

    // Add new GPIO controllers here. If a controller/plugin requires other controllers as its
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
        if (!mActiveControllers.containsKey(NodePluginType.REED) ||
            !mActiveControllers.containsKey(NodePluginType.LED)) {
          throw new RuntimeException(pluginType + " need REED and LED controllers.");
        }
        controller = ReedToLedPlugin.create(mapping, (ReedController) mActiveControllers.get
            (NodePluginType.REED).controller, (LedController) mActiveControllers.get
            (NodePluginType.LED).controller);
        handler = null;  // This plugins does not have a handler.
        break;
      case PHOTOCELL:
        controller = new PhotoCellControllerImpl(mapping, mGpioController);
        handler = new PhotoCellHandler((PhotoCellController) controller);
        break;
      default:
        throw new RuntimeException("No GPIO controller defined for valid type: " + type);
    }
    return new NodePlugin(pluginType, controller, handler);
  }

  private NodePlugin createOneWire(String type, String name) {
    NodePluginType pluginType = getPluginType(type);

    NodeController controller;
    Handler handler;

    // Add new 1-Wire based controllers here.
    switch (pluginType) {
      case DS18B20_TEMP:
        controller = new DS18B20ControllerImpl(name, mFileCreator);
        handler = new TemperatureHandler((TemperatureSensorController) controller, pluginType);
        break;
      default:
        throw new RuntimeException("No 1-wire controller defined for valid type: " + type);
    }
    return new NodePlugin(pluginType, controller, handler);
  }

  private NodePlugin createI2C(String type, int bus, int address) {
    NodePluginType pluginType = getPluginType(type);

    NodeController controller;
    Handler handler;

    // Add new 1-Wire based controllers here.
    switch (pluginType) {
      case HTU21D_TEMP_HUMID:
        Optional<NodeController> controllerOpt = HTU21DControllerImpl.create(bus, address);
        if (!controllerOpt.isPresent()) {
          throw new RuntimeException("Cannot initialize I2C controller of type: " + type);
        }
        controller = controllerOpt.get();
        handler = new TemperatureHandler((TemperatureSensorController) controller, pluginType);
        break;
      default:
        throw new RuntimeException("No I2C controller defined for valid type: " + type);
    }
    return new NodePlugin(pluginType, controller, handler);
  }

  private NodePluginType getPluginType(String type) {
    try {
      return NodePluginType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException e) {
      LOG.error("Illegal plugin name: " + type);
      throw new RuntimeException("No controller for name: " + type);
    }
  }
}
