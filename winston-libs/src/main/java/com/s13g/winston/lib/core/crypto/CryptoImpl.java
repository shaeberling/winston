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

import com.google.common.flogger.FluentLogger;

import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Common cryptographic functions, default implementation.
 */
public class CryptoImpl implements Crypto {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  private static final String ENCODING = "UTF-8";

  private final SecretKeySpec mKeySpec;

  private CryptoImpl(SecretKeySpec keySpec) {
    mKeySpec = keySpec;
  }

  public static Crypto create(byte[] key) {
    return new CryptoImpl(new SecretKeySpec(key, "AES"));
  }

  /**
   * Create a cipher for en- or decryption.
   *
   * @param mode the mode, either {@link javax.crypto.Cipher#ENCRYPT_MODE} or {@link
   * javax.crypto.Cipher#DECRYPT_MODE}
   * @param keySpec the key to use for the operation
   * @param iv if to be used for decryption, an initialization vector is needed. If a random one is
   * supposed to be used for encryption, this can be left null.
   * @return A valid Cipher implementation or null, if it Cannot be created.
   */
  @Nullable
  private static Cipher createCypher(int mode, SecretKeySpec keySpec, byte[] iv) {
    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      if (iv.length > 0) {
        cipher.init(mode, keySpec, new IvParameterSpec(iv));
      } else {
        cipher.init(mode, keySpec);
      }
      return cipher;
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
        InvalidAlgorithmParameterException e) {
      log.atSevere().withCause(e).log("Cannot create cipher");
    }
    return null;
  }

  @Override
  public Optional<EncryptedMessage> encrypt(String input) {
    try {
      return encrypt(input.getBytes(ENCODING));
    } catch (UnsupportedEncodingException e) {
      log.atSevere().withCause(e).log("Cannot encrypt");
    }
    return Optional.empty();
  }

  @Override
  public Optional<EncryptedMessage> encrypt(byte[] input) {
    Cipher cipher = createCypher(Cipher.ENCRYPT_MODE, mKeySpec, new byte[0]);
    if (cipher == null) {
      return Optional.empty();
    }

    byte[] iv;
    try {
      AlgorithmParameters params = cipher.getParameters();
      iv = params.getParameterSpec(IvParameterSpec.class).getIV();
    } catch (InvalidParameterSpecException e) {
      log.atSevere().withCause(e).log("Cannot encrypt");
      return Optional.empty();
    }

    byte[] message;
    try {
      message = cipher.doFinal(input);
      return Optional.of(new EncryptedMessage(iv, message));
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      log.atSevere().withCause(e).log("Cannot encrypt", e);
    }
    return Optional.empty();
  }

  @Override
  public Optional<byte[]> decrypt(EncryptedMessage message) {
    Cipher cipher = createCypher(Cipher.DECRYPT_MODE, mKeySpec, message.iv);
    if (cipher == null) {
      return Optional.empty();
    }

    try {
      return Optional.ofNullable(cipher.doFinal(message.message));
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      log.atSevere().withCause(e).log("Cannot decrypt");
    }
    return Optional.empty();
  }

  @Override
  public Optional<String> decryptString(EncryptedMessage message) {
    Optional<byte[]> decrypted = decrypt(message);
    if (!decrypted.isPresent()) {
      return Optional.empty();
    }
    try {
      return Optional.of(new String(decrypted.get(), ENCODING));
    } catch (UnsupportedEncodingException e) {
      log.atSevere().withCause(e).log("Cannot decrypt");
    }
    return Optional.empty();
  }
}
