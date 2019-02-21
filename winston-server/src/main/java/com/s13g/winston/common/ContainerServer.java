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

package com.s13g.winston.common;

import com.google.common.flogger.FluentLogger;

import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Starts a server to start serving the given container.
 */
public class ContainerServer {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private final int mPort;
  private final int mNumThreads;

  /**
   * Creates instances of ContainerServer.
   */
  public interface Creator {
    ContainerServer create(int port, int numThreads);
  }

  /**
   * Returns the default creator which will produce a real server. Not suitable for unit testing.
   */
  public static Creator getDefaultCreator() {
    return ContainerServer::new;
  }

  public ContainerServer(int port, int numThreads) {
    mPort = port;
    mNumThreads = numThreads;
  }

  public void startServing(Container container) {
    try {
      ContainerSocketProcessor processor =
          new ContainerSocketProcessor(container, mNumThreads);
      Connection connection = new SocketConnection(processor);
      SocketAddress address = new InetSocketAddress(mPort);
      connection.connect(address);
      log.atInfo().log("Listening at %s ", address.toString());
    } catch (IOException e) {
      log.atSevere().withCause(e).log("Cannot start webserver");
    }
  }
}
