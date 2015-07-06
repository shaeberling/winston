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

import com.google.protobuf.TextFormat;
import com.s13g.winston.master.proto.MasterProtos;

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

    private final MasterProtos.Config mConfigProto;

    private ConfigWrapper(MasterProtos.Config configProto) {
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
        MasterProtos.Config.Builder builder = MasterProtos.Config.newBuilder();
        String configStr = new String(Files.readAllBytes(configFile.toPath()));
        TextFormat.getParser().merge(configStr, builder);
        return new ConfigWrapper(builder.build());
    }

    /**
     * @return The parses config proto.
     */
    public MasterProtos.Config getConfig() {
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

        List<MasterProtos.Config.NodeMapping> nodeMappings = mConfigProto.getNodeMappingList();
        if (nodeMappings.isEmpty()) {
            throw new AssertionError("Node node mappings found");
        }

        for (MasterProtos.Config.NodeMapping nodeMapping : nodeMappings) {
            if (!nodeMapping.hasName() || nodeMapping.getName().isEmpty()) {
                throw new AssertionError("Missing node name");
            }
            if (!nodeMapping.hasAddress() || nodeMapping.getAddress().isEmpty()) {
                throw new AssertionError("Missing node address");
            }
            if (!nodeMapping.hasPort() || nodeMapping.getPort() <= 0) {
                throw new AssertionError("Invalid node port");
            }
        }
    }

    /**
     * Will print out the configuration to LOG.
     */
    public void printToLog() {
        LOG.info("Daemon Port:" + mConfigProto.getDaemonPort());
        List<MasterProtos.Config.NodeMapping> nodeMappings = mConfigProto.getNodeMappingList();
        LOG.info("Node mappings: " + nodeMappings.size());
        LOG.info("---------------------------------");
        for (MasterProtos.Config.NodeMapping nodeMapping : nodeMappings) {
            LOG.info("  Name    : " + nodeMapping.getName());
            LOG.info("  Address : " + nodeMapping.getAddress());
            LOG.info("  Port    : " + nodeMapping.getPort());
            LOG.info("  Use SSL : " + nodeMapping.getUseSsl());
            LOG.info("---------------------------------");
        }
    }
}
