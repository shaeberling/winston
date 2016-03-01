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

package com.s13g.winston.node.handler;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.s13g.winston.lib.led.LedController;
import com.s13g.winston.lib.plugin.NodePluginType;

public class LedHandler implements Handler {
  private interface LedCommandRunner {
    void runForLed(int num);
  }

  private static final Logger LOG = LogManager.getLogger(LedHandler.class);
  private final LedController mLedController;

  private enum LedCommand {
    OFF, ON
  }

  private static final LedCommand[] COMMANDS = LedCommand.values();
  private final HashMap<LedCommand, LedCommandRunner> mCommands = new HashMap<>();

  public LedHandler(LedController ledController) {
    mLedController = ledController;
    mCommands.put(LedCommand.OFF, (num) -> {
      mLedController.switchLed(num, false);
    });
    mCommands.put(LedCommand.ON, (num) -> {
      mLedController.switchLed(num, true);
    });
  }

  @Override
  public String handleRequest(String arguments) {
    // TODO: Argument validation!
    final int ledNo = Integer.parseInt(arguments.substring(0, arguments.indexOf('/')));
    final int commandNo = Integer.parseInt(arguments.substring(arguments.indexOf('/') + 1,
        arguments.length()));
    if (commandNo < 0 || commandNo >= COMMANDS.length) {
      LOG.warn("Unknown LED command: " + commandNo);
      return "FAIL";
    }
    LedCommandRunner commandRunner = mCommands.get(COMMANDS[commandNo]);
    if (commandRunner == null) {
      LOG.warn("Unmapped LED command: " + commandNo);
      return "FAIL";
    }
    commandRunner.runForLed(ledNo);
    return "OK";
  }

  @Override
  public NodePluginType getRpcName() {
    return NodePluginType.LED;
  }

}
