/*
 * Copyright 2015 Sascha Haeberling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.s13g.winston.lib.core.crypto;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

public class KeyGeneratorTest extends TestCase {

  /**
   * We cannot test easily whether the generated key is random enough, but at least we can make sure
   * that a key is successfully created and it's a different key every time.
   */
  public void testKeyGeneration() {
    final int NUM_KEYS = 10;
    Set<byte[]> generatedKeys = new HashSet<>(NUM_KEYS);

    for (int i = 0; i < NUM_KEYS; ++i) {
      byte[] newKey = KeyGenerator.generateKey();

      assertFalse("Duplicate key generated #" + i, generatedKeys.contains(newKey));
      generatedKeys.add(newKey);
    }
  }
}
