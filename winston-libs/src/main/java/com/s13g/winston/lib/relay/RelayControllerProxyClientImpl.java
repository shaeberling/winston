/*
 * Copyright 2014 Sascha Haeberling
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

package com.s13g.winston.lib.relay;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RelayControllerProxyClientImpl implements RelayController {
  private static final Logger LOG = LogManager.getLogger(RelayControllerProxyClientImpl.class);
  private final String mServerName;
  private final String mRequestFormat = "http://%s/io/relay/%d/%d";
  private final ExecutorService mExecutor = Executors.newFixedThreadPool(8);

  public RelayControllerProxyClientImpl(String serverName) {
    mServerName = serverName;
  }

  @Override
  public void switchRelay(int num, boolean on) {
    sendRpcUrl(String.format(mRequestFormat, mServerName, num, on ? 1 : 0));
  }

  @Override
  public void clickRelay(int num) {
    sendRpcUrl(String.format(mRequestFormat, mServerName, num, 2));
  }

  private void sendRpcUrl(String rpcUrl) {
    LOG.info("rpcUrl: " + rpcUrl);
    try {
      final HttpURLConnection connection = (HttpURLConnection) (new URL(rpcUrl)).openConnection();
      connection.setRequestMethod("GET");
      connection.setUseCaches(false);
      final InputStream is = connection.getInputStream();
      is.read();
      is.close();
    } catch (final MalformedURLException e1) {
      e1.printStackTrace();
    } catch (final IOException e2) {
      e2.printStackTrace();
    }
  }
}
