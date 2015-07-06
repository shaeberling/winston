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

package com.s13g.winston.node.plugin;

import com.s13g.winston.lib.plugin.NodeController;
import com.s13g.winston.lib.plugin.NodePluginType;
import com.s13g.winston.node.handler.Handler;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A node plugin with a unique name, a controller and an optional handler.
 */
@ParametersAreNonnullByDefault
public class NodePlugin {
  public final NodePluginType type;
  public final NodeController controller;
  public final Handler handler;

  public NodePlugin(NodePluginType type, NodeController controller, Handler handler) {
    this.type = type;
    this.controller = controller;
    this.handler = handler;
  }

  /**
   * @return Whether this node plugin has a handler.
   */
  public boolean hasHandler() {
    return handler != null;
  }
}
