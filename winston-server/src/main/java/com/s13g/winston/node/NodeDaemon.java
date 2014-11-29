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
import com.s13g.winston.lib.led.LedController;
import com.s13g.winston.lib.led.LedControllerFactory;
import com.s13g.winston.lib.reed.ReedController;
import com.s13g.winston.lib.reed.ReedControllerFactory;
import com.s13g.winston.lib.relay.RelayController;
import com.s13g.winston.lib.relay.RelayControllerFactory;
import com.s13g.winston.node.handler.Handler;
import com.s13g.winston.node.handler.LedHandler;
import com.s13g.winston.node.handler.ReedHandler;
import com.s13g.winston.node.handler.RelayHandler;
import com.s13g.winston.node.plugin.ReedToLedPlugin;

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
    final LedController ledController = LedControllerFactory.create(new int[] { 1, 4 },
        gpioController, null);
    final RelayController relayController = RelayControllerFactory.create(new int[] {},
        gpioController, null);
    final ReedController reedController = ReedControllerFactory.create(new int[] { 5, 6 },
        gpioController, null);

    sRegisteredHandlers = createHandlerMap(new Handler[] { new LedHandler(ledController),
        new RelayHandler(relayController), new ReedHandler(reedController) });

    new ReedToLedPlugin(ReedToLedPlugin.createMapping(new int[] { 0, 0, 1, 1 }), reedController,
        ledController);
    startServing(new NodeDaemon(), PORT, NUM_THREADS);
  }

  @Override
  public void handle(Request req, Response resp) {
    final String requestUrl = req.getAddress().toString();
    LOG.info("Request: " + requestUrl);

    String returnValue = null;
    if (requestUrl.startsWith(IO_PREFIX)) {
      returnValue = handleIoRequest(requestUrl.substring(IO_PREFIX.length()));
    }

    try {
      resp.setStatus(returnValue != null ? Status.OK : Status.NOT_FOUND);
      resp.getPrintStream().append(returnValue);
      resp.close();
    } catch (final IOException e) {
      LOG.warn("Could not deliver response");
    }
    LOG.debug("Request handled");
  }

  /**
   * Handles '/io' requests.
   *
   * @return If the request was handled this returns the return values of the
   *         handler, otherwise null is returned.
   */
  private String handleIoRequest(String command) {
    final String rpcName = command.substring(0, command.indexOf('/'));
    LOG.info("IO RPC: " + rpcName);
    if (sRegisteredHandlers.containsKey(rpcName)) {
      return sRegisteredHandlers.get(rpcName)
          .handleRequest(command.substring(rpcName.length() + 1));
    } else {
      return null;
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
      handlerMap.put(handler.getRpcName().name().toLowerCase(), handler);
    }
    return handlerMap;
  }
}
