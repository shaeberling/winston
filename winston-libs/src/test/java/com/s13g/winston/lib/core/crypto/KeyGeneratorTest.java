/*
 * Copyright 2015 The Winston Authors
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

package com.s13g.winston.lib.core.crypto;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyGeneratorTest {

  /**
   * We cannot test easily whether the generated key is random enough, but at least we can make sure
   * that a key is successfully created and it's a different key every time.
   */
  @Test
  public void testKeyGeneration() {
    final int NUM_KEYS = 10;
    Set<byte[]> generatedKeys = new HashSet<>(NUM_KEYS);

    for (int i = 0; i < NUM_KEYS; ++i) {
      byte[] newKey = KeyGenerator.instance().generateKey();

      assertFalse("Duplicate key generated #" + i, generatedKeys.contains(newKey));
      generatedKeys.add(newKey);
    }
  }

  @Test
  public void testWrongAlgorithm() {
    KeyGenerator.SecretKeyFactoryProducer throwingProducer = () -> {
      throw new NoSuchAlgorithmException("Just testing");
    };

    byte[] result = KeyGenerator.instance().generateKey(throwingProducer,
        KeyGenerator.DEFAULT_STRING_CONVERTER);
    assertEquals(0, result.length);
  }

  @Test
  public void testUnsupportedEncoding() {
    KeyGenerator.BytesToStringConverter throwingConverter = (bytes) -> {
      throw new UnsupportedEncodingException("Just testing");
    };
    byte[] result = KeyGenerator.instance().generateKey(KeyGenerator.DEFAULT_SECRECT_KEY_FACTORY,
        throwingConverter);
    assertEquals(0, result.length);
  }

  @Test
  public void testInvalidKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
    SecretKeyFactoryProxy mockedKeyFactory = mock(SecretKeyFactoryProxy.class);
    when(mockedKeyFactory.generateSecret(any())).thenThrow(new InvalidKeySpecException());

    KeyGenerator.SecretKeyFactoryProducer throwingProducer = () -> mockedKeyFactory;

    byte[] result = KeyGenerator.instance().generateKey(throwingProducer,
        KeyGenerator.DEFAULT_STRING_CONVERTER);
    assertEquals(0, result.length);
  }

  public void testCreate() {

  }
}
