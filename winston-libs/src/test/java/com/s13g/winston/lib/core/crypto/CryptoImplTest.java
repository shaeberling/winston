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

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CryptoImplTest {
  private Crypto mCrypto;
  private Crypto mCrypto2;

  @Before
  public void initialize() {
    byte[] key = KeyGenerator.instance().generateKey();
    mCrypto = CryptoImpl.create(key);
    mCrypto2 = CryptoImpl.create(key);
  }

  @Test
  public void testStringEncryption() {
    String messageIn = "This is top secret";
    Optional<EncryptedMessage> encrypted = mCrypto.encrypt(messageIn);

    assertTrue(encrypted.isPresent());

    // Any crypto with the same key should be able to decrypt the messageIn.
    Optional<String> messageOut = mCrypto2.decryptString(encrypted.get());
    assertEquals(messageIn, messageOut.get());
  }

  @Test
  public void testRawByteEncryption() {
    byte[] messageIn = new byte[]{1, 2, 3, 5, 7, 10, 42};
    Optional<EncryptedMessage> encrypted = mCrypto.encrypt(messageIn);

    assertTrue(encrypted.isPresent());

    // Any crypto with the same key should be able to decrypt the messageIn.
    Optional<byte[]> messageOut = mCrypto2.decrypt(encrypted.get());
    assertArrayEquals(messageIn, messageOut.get());
    assertFalse("Encrypted message is equals to clear text",
        Arrays.equals(messageIn, encrypted.get().message));
  }
}
