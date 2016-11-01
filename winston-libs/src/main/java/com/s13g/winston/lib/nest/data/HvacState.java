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

package com.s13g.winston.lib.nest.data;

import java.util.Optional;

/**
 * HVAC state enum.
 */
public enum HvacState {
  HEATING("heating"), COOLING("cooling"), OFF("off");
  public final String str;

  HvacState(String str) {
    this.str = str;
  }

  @Override
  public String toString() {
    return str;
  }

  /**
   * Gets the state enum from the given value string.
   */
  public static Optional<HvacState> fromString(String stateStr) {
    for (HvacState state : HvacState.values()) {
      if (state.str.equals(stateStr)) {
        return Optional.of(state);
      }
    }
    return Optional.empty();
  }
}