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

/**
 * Thrown when a module could not be created.
 */
public class ModuleCreationException extends Exception {
  private ModuleCreationException(String message) {
    super(message);
  }

  private ModuleCreationException(String message, Throwable t) {
    super(message, t);
  }

  public static ModuleCreationException create(String reason) {
    return new ModuleCreationException("Cannot create module. Reason: " + reason);
  }

  public static ModuleCreationException create(String reason, Throwable t) {
    return new ModuleCreationException("Cannot create module. Reason: " + reason, t);
  }

  public static ModuleCreationException create(Class<?> moduleClass, String reason) {
    return new ModuleCreationException(
        "Cannot create module '" + moduleClass.getSimpleName() + "'.\nReason: " + reason);
  }
}
