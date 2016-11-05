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

package com.s13g.winston.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 * Creates an SSLContext from the given keystore and password.
 */
public class SslContextCreator {
  private static final String ALGORITHM = "TLS";

  private final InputStream mKeystoreSource;
  private final String mPassword;

  /**
   * Constructs the creator.
   *
   * @param keystoreSource input stream that loads the X.509 keystore. Must contain the certificates
   * as well as private key.
   * @param password the password for the keystore and the private key.
   */
  public SslContextCreator(InputStream keystoreSource, String password) {
    mKeystoreSource = keystoreSource;
    mPassword = password;
  }

  /**
   * Creates an {@link SslContextCreator} from a keystore file name and password.
   */
  public static SslContextCreator from(String fileName,
                                        String password) throws SslContextCreationException {
    try {
      return new SslContextCreator(new FileInputStream(fileName), password);
    } catch (FileNotFoundException e) {
      throw new SslContextCreationException("Cannot find keystore file", e);
    }
  }

  /**
   * Creates an SSLContext based on the provided keystore.
   *
   * @return The context, if it could be created.
   * @throws SslContextCreationException thrown if the context cannot be created for any reason.
   */
  public SSLContext create() throws SslContextCreationException {
    KeyStore keyStore = getKeyStore(mKeystoreSource, mPassword);
    KeyManagerFactory keyManagerFactory = getKeyManagerFactory(keyStore, mPassword);
    return getSslContext(keyManagerFactory);
  }

  private static SSLContext getSslContext(KeyManagerFactory kmf) throws
      SslContextCreationException {
    try {
      SSLContext sslContext = SSLContext.getInstance(ALGORITHM);
      KeyManager[] keyManagers = kmf.getKeyManagers();
      sslContext.init(keyManagers, null, null);
      return sslContext;
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new SslContextCreationException("Cannot create SSLContext", e);
    }
  }

  private static KeyManagerFactory getKeyManagerFactory(KeyStore keyStore, String password)
      throws SslContextCreationException {
    try {
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory
          .getDefaultAlgorithm());
      keyManagerFactory.init(keyStore, password.toCharArray());
      return keyManagerFactory;
    } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
      throw new SslContextCreationException("Cannot create/init KeyManagerFactory", e);
    }
  }

  private static KeyStore getKeyStore(InputStream keystoreSource, String password) throws
      SslContextCreationException {
    try {
      KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
      keystore.load(keystoreSource, password.toCharArray());
      return keystore;
    } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
      throw new SslContextCreationException("Cannot create keystore", e);
    }
  }

  public static class SslContextCreationException extends Exception {
    private SslContextCreationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}