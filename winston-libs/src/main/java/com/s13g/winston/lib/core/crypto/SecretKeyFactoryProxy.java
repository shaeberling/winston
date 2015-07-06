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

import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;

/**
 * Some methods of SecretKeyFactory are final so the only way to mock it out is to create a
 * wrapper.
 */
public class SecretKeyFactoryProxy {
  private final SecretKeyFactory mFactory;

  public SecretKeyFactoryProxy(SecretKeyFactory factory) {
    mFactory = factory;
  }

  public SecretKey generateSecret(KeySpec spec) throws InvalidKeySpecException {
    return mFactory.generateSecret(spec);
  }
}
