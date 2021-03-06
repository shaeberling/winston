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

package com.s13g.winston.lib.winston;

/**
 * Controller that is used to communicate with Winston nodes.
 * <p>
 * TODO: Add node communication authentication/encryption
 */
public class WinstonController {

  public WinstonSensorNodeController getSensorNodeController(String address) {
    return new WinstonSensorNodeController(address);
  }

  public WinstonPowerNodeController getPowerNodeController(String address) {
    return new WinstonPowerNodeController(address);
  }

  public WinstonGarageNodeController getGarageNodeController(String address) {
    return new WinstonGarageNodeController(address);
  }

}
