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

package com.s13g.winston.lib.temperature;

import java.util.Locale;

/**
 * Represents a temperature.
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

  public Temperature(float value, Unit unit) {
    mValue = value;
    mUnit = unit;
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