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

import com.google.protobuf.TextFormat;
import com.s13g.winston.node.proto.NodeProtos;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Wrapper around the node configuration proto with some helper methods.
 */
public class ConfigWrapper {
  private static final Logger LOG = LogManager.getLogger(ConfigWrapper.class);

  private final NodeProtos.Config mConfigProto;

  public ConfigWrapper(NodeProtos.Config nodeConfigProto) {
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
    LOG.info("Reading node configuration: " + configFile.getAbsolutePath());
    NodeProtos.Config.Builder builder = NodeProtos.Config.newBuilder();
    String configStr = new String(Files.readAllBytes(configFile.toPath()));
    TextFormat.getParser().merge(configStr, builder);
    return new ConfigWrapper(builder.build());
  }

  /**
   * @return The configuration protocol buffer.
   */
  public NodeProtos.Config getConfig() {
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

    int numPlugins = mConfigProto.getGpioPluginsList().size() + mConfigProto
        .getOnewirePluginsList().size();
    if (numPlugins == 0) {
      throw new AssertionError("No active plugins found");
    }

    for (NodeProtos.Config.GpioPlugin plugin : mConfigProto.getGpioPluginsList()) {
      if (!plugin.hasType() || plugin.getType().isEmpty()) {
        throw new AssertionError("Missing plugin type");
      }
    }

    for (NodeProtos.Config.OneWirePlugin plugin : mConfigProto.getOnewirePluginsList()) {
      if (!plugin.hasType() || plugin.getType().isEmpty()) {
        throw new AssertionError("Missing plugin type");
      }
    }
  }

  /**
   * Will print out the configuration to LOG.
   */
  public void printToLog() {
    LOG.info("---------------------------------");
    LOG.info("Daemon Port:" + mConfigProto.getDaemonPort());
    List<NodeProtos.Config.GpioPlugin> gpioPluginsList = mConfigProto.getGpioPluginsList();
    LOG.info("Active GPIO plugins: " + gpioPluginsList.size());
    for (NodeProtos.Config.GpioPlugin plugin : gpioPluginsList) {
      LOG.info("  Type    : " + plugin.getType());
      LOG.info("  Mapping : " + plugin.getMappingList());
    }
    LOG.info("---------------------------------");
    List<NodeProtos.Config.OneWirePlugin> oneWirePluginsList = mConfigProto.getOnewirePluginsList();
    LOG.info("Active 1-Wire plugins: " + oneWirePluginsList.size());
    for (NodeProtos.Config.OneWirePlugin plugin : oneWirePluginsList) {
      LOG.info("  Type    : " + plugin.getType());
      LOG.info("  Name    : " + plugin.getName());
    }
    LOG.info("---------------------------------");
  }
}
