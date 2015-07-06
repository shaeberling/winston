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

package com.s13g.winston.commands;

import com.s13g.winston.util.HttpUtil;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command for opening/closing garage doors.
 */
public class GarageCommand implements Command {
    private static final Logger LOG = Logger.getLogger("GarageCommand");
    private static final String NODE_NAME = "pi-garage";
    private static final int CLICK_PARAM = 2;

    @Override
    public void execute(int num) {
        String params = String.format(RELAY_SWITCH_PARAM, num, CLICK_PARAM);
        final String url = String.format(WINSTON_SERVER_URL, NODE_NAME, params);
        try {
            HttpUtil.requestUrl(url);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Could not make request: " + e.getMessage());
        }
    }
}
