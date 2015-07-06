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

package com.s13g.winston.testing;

import com.s13g.winston.lib.core.crypto.Crypto;
import com.s13g.winston.lib.core.crypto.CryptoImpl;
import com.s13g.winston.lib.core.crypto.EncryptedMessage;
import com.s13g.winston.lib.core.crypto.KeyGenerator;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/** Application to test Crypto functionality. */
public class CryptoTestApp {
  public static void main(String[] args) {
    for (int i = 0; i < 10; ++i) {
      byte[] key = KeyGenerator.instance().generateKey();
      System.out.println("Random key of length: " + key.length);
      System.out.println(Arrays.toString(key));
    }

    String messageStr = "This is top secret.";
    byte[] key = KeyGenerator.instance().generateKey();

    Crypto crypto = CryptoImpl.create(key);
    EncryptedMessage encrypted = crypto.encrypt(messageStr);
    System.out.println("IV: " + Arrays.toString(encrypted.iv));
    System.out.println("Encrypted: " + Arrays.toString(encrypted.message));
    try {
      System.out.println("Encrypted as String: " + new String(encrypted.message, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
    }

    String decrypted = crypto.decryptString(encrypted);
    System.out.println("Decrypted: " + decrypted);
  }
}
