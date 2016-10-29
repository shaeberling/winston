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

import com.google.common.collect.ImmutableMap;
import com.s13g.winston.proto.Master;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A set of parameters that is given to a module on creation.
 */
public class ModuleParameters {
  private final Map<String, List<String>> mParameters;

  public ModuleParameters(List<Master.Parameter> parameters) {
    Map<String, List<String>> tempParameters = new HashMap<>();
    for (Master.Parameter parameter : parameters) {
      if (!tempParameters.containsKey(parameter.getName())) {
        tempParameters.put(parameter.getName(), new LinkedList<>());
      }
      tempParameters.get(parameter.getName()).add(parameter.getValue());
    }
    mParameters = ImmutableMap.copyOf(tempParameters);
  }

  /**
   * @param name the name of the parameter
   * @return If present, the value of the parameter, otherwise empty.
   */
  public Optional<List<String>> get(String name) {
    return Optional.ofNullable(mParameters.get(name));
  }
}
