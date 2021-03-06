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
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Used to generates random keys that can be used to encrypt communication using AES.
 */
public class KeyGenerator {
  /** For testing. */
  public interface SecretKeyFactoryProducer {
    SecretKeyFactoryProxy produce() throws NoSuchAlgorithmException;
  }

  /** For testing. */
  public interface BytesToStringConverter {
    String convert(byte[] bytes) throws UnsupportedEncodingException;
  }

  public static final SecretKeyFactoryProducer DEFAULT_SECRECT_KEY_FACTORY = () ->
      new SecretKeyFactoryProxy(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"));
  public static final BytesToStringConverter DEFAULT_STRING_CONVERTER =
      (bytes) -> new String(bytes, StandardCharsets.UTF_8);

  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  @Nullable
  private static KeyGenerator sInstance = null;

  public static KeyGenerator instance() {
    if (sInstance == null) {
      sInstance = new KeyGenerator();
    }
    return sInstance;
  }

  private KeyGenerator() {
  }

  /**
   * Generates random keys to use for AES encryption.
   */
  public byte[] generateKey() {
    return generateKey(DEFAULT_SECRECT_KEY_FACTORY, DEFAULT_STRING_CONVERTER);
  }

  /**
   * Visible for testing
   * <p>
   * See {@link #generateKey()}}. This can take a custom SecretKeyFactoryProducer. This is probably
   * not want you want to call.
   */
  public byte[] generateKey(SecretKeyFactoryProducer secretKeyFactoryProducer,
                            BytesToStringConverter bytesToString) {
    SecretKeyFactoryProxy factory = null;
    try {
      factory = secretKeyFactoryProducer.produce();
    } catch (NoSuchAlgorithmException e) {
      log.atSevere().withCause(e).log("Cannot not generate key");
      return new byte[0];
    }

    SecureRandom random = new SecureRandom();

    byte[] randomSalt = new byte[256];
    byte[] randomPassword = new byte[256];
    random.nextBytes(randomSalt);
    random.nextBytes(randomPassword);

    String randomPasswordStr;
    try {
      randomPasswordStr = bytesToString.convert(randomPassword);
    } catch (UnsupportedEncodingException e) {
      log.atSevere().withCause(e).log("Cannot not generate key");
      return new byte[0];
    }

    KeySpec spec = new PBEKeySpec(randomPasswordStr.toCharArray(), randomSalt, 65536, 128);

    try {
      SecretKey tmp = factory.generateSecret(spec);
      SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
      return secret.getEncoded();
    } catch (InvalidKeySpecException e) {
      log.atSevere().withCause(e).log("Cannot not generate key");
      return new byte[0];
    }
  }
}
