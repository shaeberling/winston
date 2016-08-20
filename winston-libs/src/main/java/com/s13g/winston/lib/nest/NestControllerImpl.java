/*
 * Copyright 2016 The Winston Authors
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

package com.s13g.winston.lib.nest;

import com.s13g.winston.lib.core.net.HttpUtil;
import com.s13g.winston.lib.temperature.TemperatureSensorController;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Default implementation of the Nest controller.
 */
public class NestControllerImpl implements NestController {
  private static final Logger LOG = Logger.getLogger("NestControllerImpl");
  private static final String ROOT_URL = "https://developer-api.nest.com/";
  private final String mAuthHeader;

  public NestControllerImpl(String authHeader) {
    mAuthHeader = authHeader;
  }

  public void getAllDevices() throws IOException {
    String result =
        HttpUtil.requestUrl(ROOT_URL, HttpUtil.Method.GET, HttpUtil.ContentType.JSON, mAuthHeader);
    LOG.info("Get all devices response:\n" + result);
  }

  @Override
  public TemperatureSensorController.Temperature getTemperature() {
    return null;
  }
}
