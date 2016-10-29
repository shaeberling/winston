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

package com.s13g.winston.master.config;

import com.google.common.base.Strings;
import com.google.protobuf.TextFormat;
import com.s13g.winston.proto.Master.KnownNode;
import com.s13g.winston.proto.Master.MasterConfig;
import com.s13g.winston.proto.Master.Module;
import com.s13g.winston.proto.Master.Parameter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Wrapper around the configuration proto with some helper methods.
 */
public class ConfigWrapper {
  private static Logger LOG = LogManager.getLogger(ConfigWrapper.class);

  private final MasterConfig mConfigProto;

  private ConfigWrapper(MasterConfig configProto) {
    mConfigProto = configProto;
  }

  /**
   * Parses the configuration from the given file.
   *
   * @param configFile the configuration file - must be a valid text format protocol buffer.
   * @return A config wrapper containing the new configuration.
   * @throws IOException Thrown if the configuration could not be parsed.
   */
  public static ConfigWrapper fromFile(File configFile) throws IOException {
    LOG.info("Reading master configuration: " + configFile.getAbsolutePath());
    MasterConfig.Builder builder = MasterConfig.newBuilder();
    String configStr = new String(Files.readAllBytes(configFile.toPath()));
    TextFormat.getParser().merge(configStr, builder);
    return new ConfigWrapper(builder.build());
  }

  /**
   * @return The parses config proto.
   */
  public MasterConfig getConfig() {
    return mConfigProto;
  }

  /**
   * Verifies that the proto is in good shape. If something is not sane, will
   * throw an AssertionError.
   */
  public void assertSane() {
    if (mConfigProto.getDaemonPort() <= 0) {
      throw new AssertionError("Invalid Port:" + mConfigProto.getDaemonPort());
    }

    List<Module> modules = mConfigProto.getModuleList();
    for (Module module : modules) {
      if (Strings.isNullOrEmpty(module.getType())) {
        throw new AssertionError("Module type must be set.");
      }

      for (Parameter parameter : module.getParamList()) {
        if (Strings.isNullOrEmpty(parameter.getName())) {
          throw new AssertionError("Parameter name must not be empty.");
        }
      }
    }

    for (KnownNode knownNode : mConfigProto.getKnownClientList()) {
      // TODO: Parse MAC address, check if it is valid.
      if (Strings.isNullOrEmpty(knownNode.getMacAddress())) {
        throw new AssertionError("KnownClient MAC address must be set");
      }
      if (Strings.isNullOrEmpty(knownNode.getName())) {
        throw new AssertionError("KnownClient name must be set");
      }
      if (knownNode.getPort() <= 0) {
        throw new AssertionError("KnownClient port must be > 0");
      }
      // TODO: Check if file exists and that it can be parsed.
      if (Strings.isNullOrEmpty(knownNode.getConfigFile())) {
        throw new AssertionError("KnownClient config_file must be set");
      }
    }
  }

  /**
   * Will print out the configuration to LOG.
   */
  public void printToLog() {
    LOG.info("Daemon Port:" + mConfigProto.getDaemonPort());
    List<Module> modules = mConfigProto.getModuleList();
    LOG.info("Modules: " + modules.size());
    LOG.info("---------------------------------");
    for (Module module : modules) {
      LOG.info("  Type : " + module.getType());
      for (Parameter param : module.getParamList()) {
        LOG.info("  Param : " + param.getName() + " -> " + param.getValue());
      }
      LOG.info("---------------------------------");
    }

    List<KnownNode> knownNodes = mConfigProto.getKnownClientList();
    LOG.info("Known nodes: " + knownNodes.size());
    LOG.info("---------------------------------");
    for (KnownNode knownNode : knownNodes) {
      LOG.info("  MAC Addr : " + knownNode.getMacAddress());
      LOG.info("  Name     : " + knownNode.getName());
      LOG.info("  Port     : " + knownNode.getPort());
      LOG.info("  Use SSL  : " + knownNode.getUseSsl());
      LOG.info("  Config   : " + knownNode.getConfigFile());
      LOG.info("---------------------------------");
    }
  }
}
