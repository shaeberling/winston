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
 * Away-mode enum.
 */
public enum AwayMode {
  HOME("home"), AWAY("away"), AUTO_AWAY("auto-away");
  public final String str;

  AwayMode(String str) {
    this.str = str;
  }

  @Override
  public String toString() {
    return str;
  }

  /**
   * Gets the mode enum from the given value string.
   */
  public static Optional<AwayMode> fromString(String modeStr) {
    for (AwayMode mode : AwayMode.values()) {
      if (mode.str.equals(modeStr)) {
        return Optional.of(mode);
      }
    }
    return Optional.empty();
  }
}