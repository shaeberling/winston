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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link Temperature}.
 */
public class TemperatureTest {
  @Test
  public void valuesCorrectlySet() {
    Temperature temp1 = new Temperature(-12.4f, Temperature.Unit.CELSIUS);
    assertEquals(-12.4f, temp1.get(Temperature.Unit.CELSIUS), 0f);

    Temperature temp2 = new Temperature(23.9f, Temperature.Unit.FAHRENHEIT);
    assertEquals(23.9f, temp2.get(Temperature.Unit.FAHRENHEIT), 0f);
  }

  @Test
  public void correctConversion() {
    Temperature temp1 = new Temperature(-12.4f, Temperature.Unit.CELSIUS);
    assertEquals(9.68f, temp1.get(Temperature.Unit.FAHRENHEIT), 0.01f);

    Temperature temp2 = new Temperature(23.9f, Temperature.Unit.FAHRENHEIT);
    assertEquals(-4.5f, temp2.get(Temperature.Unit.CELSIUS), 0.01f);
  }

  @Test
  public void testToString() {
    Temperature temp1 = new Temperature(-42.3f, Temperature.Unit.CELSIUS);
    assertEquals("-42.3 C", temp1.toString());

    Temperature temp2 = new Temperature(123.4f, Temperature.Unit.FAHRENHEIT);
    assertEquals("123.4 F", temp2.toString());
  }
}
