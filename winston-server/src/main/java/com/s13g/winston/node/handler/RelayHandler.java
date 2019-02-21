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


import com.google.common.base.Strings;
import com.google.common.flogger.FluentLogger;
import com.s13g.winston.lib.plugin.NodePluginType;
import com.s13g.winston.lib.relay.RelayController;

/**
 * Handles relay requests.
 */
public class RelayHandler implements Handler {
  private interface RelayCommandRunner {
    void runForRelay(int num);
  }

  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  private final RelayController mRelayController;

  private enum RelayCommand {
    OFF, ON, CLICK
  }

  private static final RelayCommand[] COMMANDS = RelayCommand.values();

  private final HashMap<RelayCommand, RelayCommandRunner> mCommands = new HashMap<>();

  public RelayHandler(RelayController relayController) {
    mRelayController = relayController;

    mCommands.put(RelayCommand.OFF, (num) -> mRelayController.switchRelay(num, false));
    mCommands.put(RelayCommand.ON, (num) -> mRelayController.switchRelay(num, true));
    mCommands.put(RelayCommand.CLICK, mRelayController::clickRelay);
  }

  @Override
  public String handleRequest(String arguments) {
    if (Strings.isNullOrEmpty(arguments)) {
      log.atWarning().log("Null or empty arguments.");
      return "FAIL";
    }

    String[] args = arguments.split("/");
    if (args.length == 0) {
      log.atWarning().log("Arguments invalid: '" + arguments + "'.");
      return "FAIL";
    }
    final int relayNo;
    try {
      relayNo = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      log.atWarning().log("Illegal relay number: '" + args[0] + "'.");
      return "FAIL";
    }

    if (args.length == 1) {
      return mRelayController.isRelayOn(relayNo) ? "1" : "0";
    }

    if (args.length > 2) {
      log.atWarning().log("Too many arguments: '" + arguments + "'.");
      return "FAIL";
    }

    // args.length == 2;
    try {
      final int commandNo = Integer.parseInt(args[1]);
      if (commandNo < 0 || commandNo >= COMMANDS.length) {
        log.atWarning().log("Unknown relay command: " + commandNo);
        return "FAIL";
      }
      RelayCommandRunner commandRunner = mCommands.get(COMMANDS[commandNo]);
      if (commandRunner == null) {
        log.atWarning().log("Unmapped relay command: " + commandNo);
        return "FAIL";
      }
      commandRunner.runForRelay(relayNo);
      return "OK";

    } catch (NumberFormatException e) {
      log.atWarning().log("Illegal command number: '" + args[1] + "'.");
      return "FAIL";
    }
  }

  @Override
  public NodePluginType getRpcName() {
    return NodePluginType.RELAY;
  }
}
