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

package com.s13g.winston.master.modules;

import com.s13g.winston.master.channel.Channel;

import java.util.List;

/**
 * The common interface of a master module.
 */
public interface Module {
  /**
   * Initializes a module with the given parameters.
   *
   * @param params the parameters for this module.
   * @throws ModuleInitException if the module could not be initialized.
   */
  void initialize(ModuleParameters params) throws ModuleInitException;

  /**
   * @return The type of module which identifies it uniquly.
   */
  String getType();

  /**
   * Creates and returns all channel from this module.
   */
  List<Channel> getChannels();

  /**
   * Thrown if the module could not be created.
   */
  class ModuleInitException extends Exception {
    public ModuleInitException(String message) {
      super(message);
    }
  }
}
