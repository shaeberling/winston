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

/**
 * Interface for the cryptographic functions.
 */
public interface Crypto {
  /** Encrypts the given string. */
  public EncryptedMessage encrypt(String input);

  /** Encrypts the given data. */
  public EncryptedMessage encrypt(byte[] input);

  /** Decrypts the given message and returns the raw data. */
  public byte[] decrypt(EncryptedMessage message);

  /** Decrypts the given message and returns it as a UTF-8 string. */
  public String decryptString(EncryptedMessage message);
}
