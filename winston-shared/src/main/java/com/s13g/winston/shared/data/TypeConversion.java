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

package com.s13g.winston.shared.data;

/**
 * Helper to do basic type conversion.
 */
public final class TypeConversion {

  private TypeConversion() {
  }

  /** Converts strings to boolean. */
  public static boolean stringToBoolean(String value) throws IllegalFormatException {
    value = value.toLowerCase();
    if ("0".equals(value) || "false".equals(value)) {
      return false;
    } else if ("1".equals(value) || "true".equals(value)) {
      return true;
    } else {
      throw new IllegalFormatException("Illegal boolean value: '" + value + "'.");
    }
  }

  /** Converts a boolean to a string for the winston API. */
  public static String booleanToString(Boolean value) {
    return value == null || !value ? "0" : "1";
  }

  public static class IllegalFormatException extends Exception {
    IllegalFormatException(String messge) {
      super(messge);
    }
  }
}
