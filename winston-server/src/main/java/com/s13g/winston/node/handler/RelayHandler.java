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

package com.s13g.winston.node.handler;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.s13g.winston.lib.relay.RelayController;

/**
 * Handles relay requests.
 */
public class RelayHandler implements Handler {
  private static interface RelayCommandRunner {
    public void runForRelay(int num);
  }

  private static final Logger LOG = LogManager.getLogger(RelayHandler.class);
  private final RelayController mRelayController;

  private static enum RelayCommand {
    OFF, ON, CLICK
  }

  private static final RelayCommand[] COMMANDS = RelayCommand.values();

  private final HashMap<RelayCommand, RelayCommandRunner> mCommands = new HashMap<>();

  public RelayHandler(RelayController relayController) {
    mRelayController = relayController;

    mCommands.put(RelayCommand.OFF, (num) -> {
      mRelayController.switchRelay(num, false);
    });
    mCommands.put(RelayCommand.ON, (num) -> {
      mRelayController.switchRelay(num, true);
    });
    mCommands.put(RelayCommand.CLICK, (num) -> {
      mRelayController.clickRelay(num);
    });
  }

  @Override
  public String handleRequest(String arguments) {
    // TODO: Argument validation!
    final int relayNo = Integer.parseInt(arguments.substring(0, arguments.indexOf('/')));
    final int commandNo = Integer.parseInt(arguments.substring(arguments.indexOf('/') + 1,
        arguments.length()));
    if (commandNo < 0 || commandNo >= COMMANDS.length) {
      LOG.warn("Unknown relay command: " + commandNo);
      return "FAIL";
    }
    mCommands.get(COMMANDS[commandNo]).runForRelay(relayNo);
    return "OK";
  }

  @Override
  public HandlerType getRpcName() {
    return HandlerType.RELAY;
  }
}
