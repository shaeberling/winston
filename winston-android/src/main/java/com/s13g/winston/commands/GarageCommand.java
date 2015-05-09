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

package com.s13g.winston.commands;

import com.s13g.winston.HttpUtil;

/**
 * Command for opening/closing garage doors.
 */
public class GarageCommand implements Command {
    private static final String GARAGE_SERVER_URL = "http://heights.s13g.com:1984/io/%s";
//    private static final String GARAGE_SERVER_URL = "http://192.168.1.201:1984/io/%s";
    private static final int CLICK_PARAM = 2;

    @Override
    public void execute(int num) {
        String params = String.format(RELAY_SWITCH_PARAM, num, CLICK_PARAM);
        final String url = String.format(GARAGE_SERVER_URL, params);
        HttpUtil.requestUrl(url);
    }
}
