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

package com.s13g.winston.node.config;

import com.google.common.flogger.FluentLogger;
import com.google.protobuf.TextFormat;
import com.s13g.winston.proto.Node.NodeConfig;




import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Wrapper around the node configuration proto with some helper methods.
 */
public class ConfigWrapper {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private final NodeConfig mConfigProto;

  private ConfigWrapper(NodeConfig nodeConfigProto) {
    mConfigProto = nodeConfigProto;
  }

  /**
   * Parses the configuration from the given file.
   *
   * @param configFile the configuration file - must be a valid text format protocol buffer.
   * @return A config wrapper containing the new configuration.
   * @throws IOException Thrown if the configuration could not be parsed.
   */
  public static ConfigWrapper fromFile(File configFile) throws IOException {
    log.atInfo().log("Reading node configuration: " + configFile.getAbsolutePath());
    NodeConfig.Builder builder = NodeConfig.newBuilder();
    String configStr = new String(Files.readAllBytes(configFile.toPath()));
    TextFormat.getParser().merge(configStr, builder);
    return new ConfigWrapper(builder.build());
  }

  /**
   * @return The configuration protocol buffer.
   */
  public NodeConfig getConfig() {
    return mConfigProto;
  }

  /**
   * Verifies that the proto is in good shape. If something is not sane, will throw an
   * AssertionError.
   */
  public void assertSane() {
    if (mConfigProto.getDaemonPort() <= 0) {
      throw new AssertionError("Invalid Port:" + mConfigProto.getDaemonPort());
    }

    int numPlugins = mConfigProto.getGpioPluginsList().size() +
        mConfigProto.getOnewirePluginsList().size() +
        mConfigProto.getI2CPluginsList().size();
    if (numPlugins == 0) {
      throw new AssertionError("No active plugins found");
    }

    for (NodeConfig.GpioPlugin plugin : mConfigProto.getGpioPluginsList()) {
      if (plugin.getType() == null || plugin.getType().isEmpty()) {
        throw new AssertionError("Missing plugin type");
      }
    }

    for (NodeConfig.OneWirePlugin plugin : mConfigProto.getOnewirePluginsList()) {
      if (plugin.getType() == null || plugin.getType().isEmpty()) {
        throw new AssertionError("Missing plugin type");
      }
    }
  }

  /**
   * Will print out the configuration to LOG.
   */
  public void printToLog() {
    log.atInfo().log("---------------------------------");
    log.atInfo().log("Daemon Port:" + mConfigProto.getDaemonPort());
    List<NodeConfig.GpioPlugin> gpioPluginsList = mConfigProto.getGpioPluginsList();
    log.atInfo().log("Active GPIO plugins: " + gpioPluginsList.size());
    for (NodeConfig.GpioPlugin plugin : gpioPluginsList) {
      log.atInfo().log("  Type    : " + plugin.getType());
      log.atInfo().log("  Mapping : " + plugin.getMappingList());
    }
    log.atInfo().log("---------------------------------");
    List<NodeConfig.OneWirePlugin> oneWirePluginsList = mConfigProto.getOnewirePluginsList();
    log.atInfo().log("Active 1-Wire plugins: " + oneWirePluginsList.size());
    for (NodeConfig.OneWirePlugin plugin : oneWirePluginsList) {
      log.atInfo().log("  Type    : " + plugin.getType());
      log.atInfo().log("  Name    : " + plugin.getName());
    }
    log.atInfo().log("---------------------------------");
    List<NodeConfig.I2cPlugin> i2cPluginsList = mConfigProto.getI2CPluginsList();
    log.atInfo().log("Active I2C plugins: " + i2cPluginsList.size());
    for (NodeConfig.I2cPlugin plugin : i2cPluginsList) {
      log.atInfo().log("  Type    : " + plugin.getType());
      log.atInfo().log("  Bus     : " + plugin.getBus());
      log.atInfo().log("  Addr    : " + plugin.getAddress());
    }
    log.atInfo().log("---------------------------------");
  }
}
