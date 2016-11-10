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
 * Tests for {@link com.s13g.winston.shared.data.Temperature}.
 */
public class TemperatureTest {
  @Test
  public void valuesCorrectlySet() {
    com.s13g.winston.shared.data.Temperature temp1 = new com.s13g.winston.shared.data.Temperature(-12.4f, com.s13g.winston.shared.data.Temperature.Unit.CELSIUS);
    assertEquals(-12.4f, temp1.get(com.s13g.winston.shared.data.Temperature.Unit.CELSIUS), 0f);

    com.s13g.winston.shared.data.Temperature temp2 = new com.s13g.winston.shared.data.Temperature(23.9f, com.s13g.winston.shared.data.Temperature.Unit.FAHRENHEIT);
    assertEquals(23.9f, temp2.get(com.s13g.winston.shared.data.Temperature.Unit.FAHRENHEIT), 0f);
  }

  @Test
  public void correctConversion() {
    com.s13g.winston.shared.data.Temperature temp1 = new com.s13g.winston.shared.data.Temperature(-12.4f, com.s13g.winston.shared.data.Temperature.Unit.CELSIUS);
    assertEquals(9.68f, temp1.get(com.s13g.winston.shared.data.Temperature.Unit.FAHRENHEIT), 0.01f);

    com.s13g.winston.shared.data.Temperature temp2 = new com.s13g.winston.shared.data.Temperature(23.9f, com.s13g.winston.shared.data.Temperature.Unit.FAHRENHEIT);
    assertEquals(-4.5f, temp2.get(com.s13g.winston.shared.data.Temperature.Unit.CELSIUS), 0.01f);
  }

  @Test
  public void testToString() {
    com.s13g.winston.shared.data.Temperature temp1 = new com.s13g.winston.shared.data.Temperature(-42.3f, com.s13g.winston.shared.data.Temperature.Unit.CELSIUS);
    assertEquals("-42.3 C", temp1.toString());

    com.s13g.winston.shared.data.Temperature temp2 = new com.s13g.winston.shared.data.Temperature(123.4f, com.s13g.winston.shared.data.Temperature.Unit.FAHRENHEIT);
    assertEquals("123.4 F", temp2.toString());
  }
}
