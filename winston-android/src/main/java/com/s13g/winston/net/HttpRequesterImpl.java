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

package com.s13g.winston.net;

import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.s13g.winston.PreferenceManager;
import com.s13g.winston.R;
import com.s13g.winston.views.Toaster;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation for making HTTP requests.
 */
public class HttpRequesterImpl implements HttpRequester {
  private static final Logger LOG = Logger.getLogger("HttpRequester");

  private final String mRequestPattern;

  public HttpRequesterImpl(String server, String authToken) {
    mRequestPattern = server + "%s?authtoken=" + authToken;
  }

  public static Optional<HttpRequester> create(PreferenceManager prefManager, Toaster toaster) {
    Optional<String> server = prefManager.getServer();
    if (!server.isPresent()) {
      toaster.showToast(R.string.no_server_pref, Toaster.Duration.LONG);
      return Optional.absent();
    }
    Optional<String> authtoken = prefManager.getAuthtoken();
    if (!authtoken.isPresent()) {
      toaster.showToast(R.string.no_auth_token_pref, Toaster.Duration.LONG);
      return Optional.absent();
    }
    HttpRequester httpRequester = new HttpRequesterImpl(server.get(), authtoken.get());
    return Optional.of(httpRequester);
  }

  @Override
  public byte[] request(String path) throws IOException {
    try {
      String url = String.format(mRequestPattern, path);
      LOG.info("Making request to: '" + url + "'.");
      URLConnection conn = new URL(url).openConnection();
      conn.setUseCaches(false);
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      long bytesRead = ByteStreams.copy(conn.getInputStream(), buffer);
      LOG.info("Read " + bytesRead + " bytes.");
      return buffer.toByteArray();
    } catch (Throwable e) {
      LOG.log(Level.SEVERE, "HTTP request failed ... ", e);
      throw new IOException("HTTP request failed.", e);
    }
  }

  @Override
  public ListenableFuture<byte[]> requestAsync(final String url, Executor executor) {
    final SettableFuture<byte[]> future = SettableFuture.create();
    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          future.set(request(url));
        } catch (IOException e) {
          future.setException(e);
        }
      }
    });
    return future;
  }
}
