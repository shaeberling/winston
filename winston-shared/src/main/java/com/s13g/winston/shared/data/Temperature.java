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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.Locale;

/**
 * Represents a temperature value. Can be parsed and put into a string.
 */
public class Temperature {
  public enum Unit {
    CELSIUS("%s C"), FAHRENHEIT("%s F");

    final String mPattern;

    Unit(String pattern) {
      mPattern = pattern;
    }
  }

  private final float mValue;
  private final Unit mUnit;

  /**
   * Parses the string into a temperature object.
   *
   * @param tempStr the string, like "20 C" or "69 F".
   * @return The temperature object.
   * @throws IllegalArgumentException thrown if the string cannot be parsed.
   */
  public static Temperature parse(String tempStr) throws IllegalArgumentException {
    if (Strings.isNullOrEmpty(tempStr)) {
      throw new IllegalArgumentException("Cannot parse to temperature: '" + tempStr + "'.");
    }
    String[] parts = tempStr.toLowerCase().trim().split(" ");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Cannot parse to temperature: '" + tempStr + "'.");
    }
    float number;
    try {
      number = Float.parseFloat(parts[0]);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Cannot parse to temperature: '" + tempStr + "'.");
    }

    switch (parts[1]) {
      case "c":
        return new Temperature(number, Unit.CELSIUS);
      case "f":
        return new Temperature(number, Unit.FAHRENHEIT);
      default:
        throw new IllegalArgumentException("Cannot parse to temperature: '" + tempStr + "'.");
    }
  }

  public Temperature(float value, Unit unit) {
    mValue = value;
    mUnit = unit;
  }

  public float getRounded(Unit unit) {
    return Math.round(get(unit) * 100.0f) / 100.0f;
  }

  /** Returns the temperature value in the given unit. */
  public float get(Unit unit) {
    if (mUnit == unit) {
      return mValue;
    }

    if (unit == Unit.CELSIUS && mUnit == Unit.FAHRENHEIT) {
      return (mValue - 32) / 1.8f;
    } else if (unit == Unit.FAHRENHEIT && mUnit == Unit.CELSIUS) {
      return mValue * 1.8f + 32;
    }
    throw new RuntimeException("Unsupported temperature conversion");
  }

  @Override
  public String toString() {
    return String.format(Locale.getDefault(), mUnit.mPattern, mValue);
  }
}