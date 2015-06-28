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

package com.s13g.winston.node;

import com.s13g.winston.node.config.ConfigWrapper;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The node daemon is the main executable that is started on a Winston node.
 */
@ParametersAreNonnullByDefault
public class NodeDaemon {
  private static final int NUM_THREADS = 4;
  private static Logger LOG = LogManager.getLogger(NodeDaemon.class);

  public static void main(final String... args) throws IOException {
    File configFile = new File("node.config");
    if (!configFile.exists() || !configFile.isFile() || !configFile.canRead()) {
      LOG.log(Level.ERROR, "Cannot read config file: " + configFile.getAbsolutePath());
      return;
    }
    LOG.info("Node starting up ...");

    ConfigWrapper configWrapper = ConfigWrapper.fromFile(configFile);
    configWrapper.printToLog();
    configWrapper.assertSane();


    NodeContainer container = NodeContainer.from(configWrapper.getConfig());
    container.startServing(NUM_THREADS);
  }
}
