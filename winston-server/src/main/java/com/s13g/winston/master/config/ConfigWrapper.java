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

import com.google.common.flogger.FluentLogger;
import com.google.protobuf.TextFormat;
import com.s13g.winston.proto.Master;
import com.s13g.winston.proto.Master.Channel;
import com.s13g.winston.proto.Master.KnownNode;
import com.s13g.winston.proto.Master.MasterConfig;
import com.s13g.winston.proto.Master.Module;
import com.s13g.winston.proto.Master.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Wrapper around the configuration proto with some helper methods.
 */
public class ConfigWrapper {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

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
    log.atInfo().log("Reading master configuration: " + configFile.getAbsolutePath());
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
   * Verifies that the proto is in good shape. If something is not sane, will throw an
   * AssertionError.
   */
  public void assertSane() {
    if (mConfigProto.getDaemonPort() <= 0) {
      throw new AssertionError("Invalid Port:" + mConfigProto.getDaemonPort());
    }

    List<Module> modules = mConfigProto.getModuleList();
    for (Module module : modules) {
      if (isNullOrEmpty(module.getType())) {
        throw new AssertionError("Module type must be set.");
      }

      for (Channel channel : module.getChannelList()) {
        if (isNullOrEmpty(channel.getType())) {
          throw new AssertionError("Channel type must be set.");
        }
        for (Parameter parameter : channel.getParameterList()) {
          if (isNullOrEmpty(parameter.getName())) {
            throw new AssertionError("Parameter name must not be empty.");
          }
        }
      }
    }

    for (KnownNode knownNode : mConfigProto.getKnownClientList()) {
      // TODO: Parse MAC address, check if it is valid.
      if (isNullOrEmpty(knownNode.getMacAddress())) {
        throw new AssertionError("KnownClient MAC address must be set");
      }
      if (isNullOrEmpty(knownNode.getName())) {
        throw new AssertionError("KnownClient name must be set");
      }
      if (knownNode.getPort() <= 0) {
        throw new AssertionError("KnownClient port must be > 0");
      }
      // TODO: Check if file exists and that it can be parsed.
      if (isNullOrEmpty(knownNode.getConfigFile())) {
        throw new AssertionError("KnownClient config_file must be set");
      }
    }

    if (!isNullOrEmpty(mConfigProto.getSslKeystorePath()) &&
        isNullOrEmpty(mConfigProto.getSslKeystorePassword())) {
      throw new AssertionError("SSL keystore given without password");
    }

    if (mConfigProto.getAuthClientCount() == 0) {
      throw new AssertionError("Not authenticated clients set. Nobody would have access.");
    }
  }

  /**
   * Will print out the configuration to LOG.
   */
  public void printToLog() {
    log.atInfo().log("Daemon Port     :" + mConfigProto.getDaemonPort());
    log.atInfo().log("Keystore        :" + mConfigProto.getSslKeystorePath());
    log.atInfo().log("Keystore passwd : " + (isNullOrEmpty(mConfigProto.getSslKeystorePassword())
        ? "<not given>" : "<given>"));
    log.atInfo().log("Auth clients    :" + mConfigProto.getAuthClientCount());
    List<Module> modules = mConfigProto.getModuleList();
    log.atInfo().log("Modules         : " + modules.size());
    log.atInfo().log("---------------------------------");
    for (Module module : modules) {
      log.atInfo().log("  Type : " + module.getType());
      for (Channel channel : module.getChannelList()) {
        log.atInfo().log("    Type   : " + channel.getType());
        log.atInfo().log("    Addr   : " + channel.getAddress());
        for (Parameter param : channel.getParameterList()) {
          log.atInfo().log("    Param   : " + param.getName() + " -> " + param.getValue());
        }
        log.atInfo().log("---------------------------------");
      }
      log.atInfo().log("---------------------------------");
    }

    List<Master.Group> groups = mConfigProto.getGroupList();
    log.atInfo().log("Groups defined: " + groups.size());
    for (Master.Group group : groups) {
      log.atInfo().log("  Name    : " + group.getName());
      log.atInfo().log("  Triggers: " + group.getTriggerCount());
    }

    List<KnownNode> knownNodes = mConfigProto.getKnownClientList();
    log.atInfo().log("Known nodes: " + knownNodes.size());
    log.atInfo().log("---------------------------------");
    for (KnownNode knownNode : knownNodes) {
      log.atInfo().log("  MAC Addr : " + knownNode.getMacAddress());
      log.atInfo().log("  Name     : " + knownNode.getName());
      log.atInfo().log("  Port     : " + knownNode.getPort());
      log.atInfo().log("  Use SSL  : " + knownNode.getUseSsl());
      log.atInfo().log("  Config   : " + knownNode.getConfigFile());
      log.atInfo().log("---------------------------------");
    }
  }
}
