/*
 * Copyright 2015 Sascha Haeberling
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

package com.s13g.winston.requests;

import com.s13g.winston.commands.Command;
import com.s13g.winston.commands.GarageCommand;
import com.s13g.winston.commands.LightCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Makes requests to the nodes.
 */
public class NodeRequests implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger("NodeRequests");
    private static final Map<String, Command> sCommandClasses = new HashMap<>();

    static {
        sCommandClasses.put("light", new LightCommand());
        sCommandClasses.put("garage", new GarageCommand());
    }

    private final ExecutorService mExecutors;

    public NodeRequests() {
        mExecutors = Executors.newSingleThreadExecutor();
    }

    public NodeRequests(ExecutorService executors) {
        mExecutors = executors;
    }

    /**
     * Executes request to node.
     *
     * @param command e.g. '/light/0' or '/garage/1'
     */
    public void execute(String command) {
        if (!command.startsWith("/")) {
            LOG.warning("Command not legal: " + command);
            return;
        }

        int posNextSlash = command.indexOf("/", 2);
        final String commandName = command.substring(1, posNextSlash);
        LOG.info("Command: " + commandName);

        String argument = command.substring(posNextSlash + 1, command.length());
        final int argumentNum = Integer.parseInt(argument);
        LOG.info("Argument: " + argumentNum);

        Future lastTask = mExecutors.submit(new Runnable() {
            @Override
            public void run() {
                sCommandClasses.get(commandName).execute(argumentNum);
            }
        });
    }

    @Override
    public void close() {
        mExecutors.shutdown();
    }
}
