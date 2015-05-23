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

package com.s13g.winston.node.handler;

import com.s13g.winston.lib.led.LedController;
import com.s13g.winston.lib.plugin.NodeController;
import com.s13g.winston.lib.reed.ReedController;
import com.s13g.winston.lib.relay.RelayController;

/**
 * Creates a handler bases on the controller type.
 */
public class HandlerCreator {
  /**
   * Given a controller, creates and returns its handler, if one exists.
   */
  public static Handler create(NodeController controller) {
    switch (controller.getType()) {
      case LED:
        return new LedHandler((LedController) controller);
      case REED:
        return new ReedHandler((ReedController) controller);
      case RELAY:
        return new RelayHandler((RelayController) controller);
      default:
        return null;
    }
  }
}
