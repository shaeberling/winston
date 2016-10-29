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

import com.s13g.winston.master.ModuleContext;

/**
 * Creates modules. Duh!
 */
public interface ModuleCreator<T extends Module> {
  /**
   * @return A unique name across all modules that identified it.
   */
  String getType();

  /** Takes parameters to crate a new module of type T. */
  T create(ModuleContext context, ModuleParameters parameters) throws ModuleCreationException;
}