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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;

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
  private static final Logger LOG = LogManager.getLogger(CryptoImpl.class);
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
   * @param mode    the mode, either {@link javax.crypto.Cipher#ENCRYPT_MODE} or {@link
   *                javax.crypto.Cipher#DECRYPT_MODE}
   * @param keySpec the key to use for the operation
   * @param iv      if to be used for decryption, an initialization vector is needed. If a random
   *                one is supposed to be used for encryption, this can be left null.
   * @return A valid Cipher implementation or null, if it could not be created.
   */
  private static Cipher createCypher(int mode, SecretKeySpec keySpec, byte[] iv) {
    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      if (iv != null) {
        cipher.init(mode, keySpec, new IvParameterSpec(iv));
      } else {
        cipher.init(mode, keySpec);
      }
      return cipher;
    } catch (NoSuchAlgorithmException e) {
      LOG.error("Could not create cipher", e);
    } catch (NoSuchPaddingException e) {
      LOG.error("Could not create cipher", e);
    } catch (InvalidKeyException e) {
      LOG.error("Could not create cipher", e);
    } catch (InvalidAlgorithmParameterException e) {
      LOG.error("Could not create cipher", e);
    }
    return null;
  }

  @Override
  public EncryptedMessage encrypt(String input) {
    try {
      return encrypt(input.getBytes(ENCODING));
    } catch (UnsupportedEncodingException e) {
      LOG.error("Could not encrypt", e);
    }
    return null;
  }

  @Override
  public EncryptedMessage encrypt(byte[] input) {
    Cipher cipher = createCypher(Cipher.ENCRYPT_MODE, mKeySpec, null);
    if (cipher == null) {
      return null;
    }

    byte[] iv;
    try {
      AlgorithmParameters params = cipher.getParameters();
      iv = params.getParameterSpec(IvParameterSpec.class).getIV();
    } catch (InvalidParameterSpecException e) {
      LOG.error("Could not encrypt", e);
      return null;
    }

    byte[] message;
    try {
      message = cipher.doFinal(input);
      return new EncryptedMessage(iv, message);
    } catch (IllegalBlockSizeException e) {
      LOG.error("Could not encrypt", e);
    } catch (BadPaddingException e) {
      LOG.error("Could not encrypt", e);
    }
    return null;
  }

  @Override
  public byte[] decrypt(EncryptedMessage message) {
    Cipher cipher = createCypher(Cipher.DECRYPT_MODE, mKeySpec, message.iv);
    if (cipher == null) {
      return null;
    }

    try {
      return cipher.doFinal(message.message);
    } catch (IllegalBlockSizeException e) {
      LOG.error("Could not decrypt", e);
    } catch (BadPaddingException e) {
      LOG.error("Could not decrypt", e);
    }
    return null;
  }

  @Override
  public String decryptString(EncryptedMessage message) {
    byte[] decrypted = decrypt(message);
    if (decrypted == null) {
      return null;
    }
    try {
      return new String(decrypted, ENCODING);
    } catch (UnsupportedEncodingException e) {
      LOG.error("Could not decrypt", e);
    }
    return null;
  }
}
