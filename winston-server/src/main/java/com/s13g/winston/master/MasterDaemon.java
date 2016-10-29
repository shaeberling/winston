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

package com.s13g.winston.master;

import com.s13g.winston.lib.core.util.HttpRequesterImpl;
import com.s13g.winston.lib.core.util.concurrent.HttpRequester;
import com.s13g.winston.master.config.ConfigWrapper;
import com.s13g.winston.master.modules.ModuleRegistry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * The master daemon executable of the Winston home automation system.
 */
public class MasterDaemon {
  private static final Logger LOG = LogManager.getLogger(MasterDaemon.class);
  private static int NUM_HTTP_THREADS = 8;

  public static void main(final String... args) throws IOException {
    File configFile = new File("master.config");
    if (!configFile.exists() || !configFile.isFile() || !configFile.canRead()) {
      LOG.log(Level.ERROR, "Cannot read config file: " + configFile.getAbsolutePath());
      return;
    }

    // Load and print out configuration.
    ConfigWrapper configWrapper = ConfigWrapper.fromFile(configFile);
    configWrapper.printToLog();
    configWrapper.assertSane();

    ModuleContext moduleContext = new ModuleContext();
    ModuleRegistry moduleRegistry = new ModuleRegistry(moduleContext, configWrapper.getConfig());

    int port = configWrapper.getConfig().getDaemonPort();
    HttpRequester httpRequester = new HttpRequesterImpl();
    MasterContainer httpContainer =
        new MasterContainer(port, null, httpRequester);
    httpContainer.startServing(NUM_HTTP_THREADS);
  }
}
