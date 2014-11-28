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

package com.s13g.winston.node;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.s13g.winston.lib.core.Provider;
import com.s13g.winston.lib.core.SingletonProvider;
import com.s13g.winston.lib.relay.RelayController;
import com.s13g.winston.lib.relay.RelayControllerFactory;
import com.s13g.winston.node.handler.Handler;
import com.s13g.winston.node.handler.RelayHandler;

/**
 * The node daemon is the main executable that is started on a Winston node.
 */
public class NodeDaemon implements Container {
  private static Logger LOG = LogManager.getLogger(NodeDaemon.class);
  private static final int NUM_THREADS = 4;
  private static final int PORT = 1984;
  private static final String IO_PREFIX = "/io/";
  private static HashMap<String, Handler> sRegisteredHandlers;

  public static void main(final String... args) throws IOException {
    LOG.info("Node starting up...");

    final Provider<GpioController> gpioController = SingletonProvider.from(() -> GpioFactory
        .getInstance());
    // TODO: Depending on configuration file, different modules need to be
    // loaded and configuration needs to be forwarded to them.
    final RelayController relayController = RelayControllerFactory.create(new int[] { 1, 2, 3, 4 },
        gpioController, null);
    sRegisteredHandlers = createHandlerMap(new Handler[] { new RelayHandler(relayController) });
    startServing(new NodeDaemon(), PORT, NUM_THREADS);
  }

  @Override
  public void handle(Request req, Response resp) {
    final String requestUrl = req.getAddress().toString();
    LOG.info("Request: " + requestUrl);

    boolean handled = false;
    if (requestUrl.startsWith(IO_PREFIX)) {
      handled = handleIoRequest(requestUrl.substring(IO_PREFIX.length()));
    }

    try {
      resp.setStatus(handled ? Status.OK : Status.NOT_FOUND);
      resp.getPrintStream().append(handled ? "OK" : "NOK");
      resp.close();
    } catch (final IOException e) {
      LOG.warn("Could not deliver response");
    }
    LOG.debug("Request handled");
  }

  /**
   * Handles '/io' requests.
   *
   * @return Whether the request was handled. TODO: Have to find a way to
   *         communicate error. Maybe through exceptions?
   */
  private boolean handleIoRequest(String command) {
    final String rpcName = command.substring(0, command.indexOf('/'));
    LOG.info("IO RPC: " + rpcName);
    if (sRegisteredHandlers.containsKey(rpcName)) {
      sRegisteredHandlers.get(rpcName).handleRequest(command.substring(rpcName.length() + 1));
      return true;
    } else {
      return false;
    }
  }

  private static void startServing(Container container, int port, int numThreads)
      throws IOException {
    final ContainerSocketProcessor processor = new ContainerSocketProcessor(container, numThreads);

    // Since this server will run forever, no need to close connection.
    @SuppressWarnings("resource")
    final Connection connection = new SocketConnection(processor);
    final SocketAddress address = new InetSocketAddress(port);
    LOG.info("Listening to: " + address.toString());
    connection.connect(address);
  }

  private static HashMap<String, Handler> createHandlerMap(Handler[] handlers) {
    final HashMap<String, Handler> handlerMap = new HashMap<>();
    for (final Handler handler : handlers) {
      handlerMap.put(handler.getRpcName(), handler);
    }
    return handlerMap;
  }
}
