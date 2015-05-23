/*
 * Copyright 2015 Sascha Haeberling
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

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.s13g.winston.lib.core.Provider;
import com.s13g.winston.lib.core.SingletonProvider;
import com.s13g.winston.node.handler.Handler;
import com.s13g.winston.lib.plugin.NodeController;
import com.s13g.winston.lib.plugin.NodeControllerCreator;
import com.s13g.winston.node.handler.HandlerCreator;
import com.s13g.winston.node.proto.NodeProtos;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Container for serving the node HTTP requests from.
 */
public class NodeContainer implements Container {
  private static final Logger LOG = LogManager.getLogger(NodeContainer.class);

  private static final String IO_PREFIX = "/io/";

  private final int mPort;
  private HashMap<String, Handler> mRegisteredHandlers;


  NodeContainer(int port, HashMap<String, Handler> registeredHandlers) {
    mPort = port;
    mRegisteredHandlers = registeredHandlers;
  }

  /**
   * Creates and returns a container based on the given configuration.
   *
   * @param config the configuration to be used for this container.
   * @return The valid container to serve the master requests.
   */
  public static NodeContainer from(NodeProtos.Config config) {
    final Provider<GpioController> gpioController = SingletonProvider.from(GpioFactory::getInstance);
    NodeControllerCreator nodeControllerCreator = new NodeControllerCreator(gpioController
        .provide());
    List<Handler> activeHandlers = new ArrayList<>();

    // For each configured plugin we instantiate the controller and its handler, if existing.
    // NOTE: The order is important since some plugins might depend on other controllers and
    // therefore need to be instantiated later.
    for (NodeProtos.Config.Plugin activePlugin : config.getActivePluginsList()) {
      String name = activePlugin.getName();
      int[] mapping = activePlugin.getMappingList().stream().mapToInt(i -> i).toArray();

      NodeController controller = nodeControllerCreator.create(name, mapping);
      Handler handler = HandlerCreator.create(controller);
      if (handler != null) {
        // Add all active handlers so we can forward HTTP requests to it.
        activeHandlers.add(handler);
      }
    }
    return new NodeContainer(config.getDaemonPort(), createHandlerMap(activeHandlers));
  }

  private static HashMap<String, Handler> createHandlerMap(List<Handler> handlers) {
    final HashMap<String, Handler> handlerMap = new HashMap<>();
    for (final Handler handler : handlers) {
      handlerMap.put(handler.getRpcName().name().toLowerCase(), handler);
    }
    return handlerMap;
  }

  public void startServing(int numThreads)
      throws IOException {
    final ContainerSocketProcessor processor = new ContainerSocketProcessor(this, numThreads);

    // Since this server will run forever, no need to close connection.
    @SuppressWarnings("resource")
    final Connection connection = new SocketConnection(processor);
    final SocketAddress address = new InetSocketAddress(mPort);
    LOG.info("Listening to: " + address.toString());
    connection.connect(address);
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
   * handler, otherwise null is returned.
   */
  private String handleIoRequest(String command) {
    final String rpcName = command.substring(0, command.indexOf('/'));
    LOG.info("IO RPC: " + rpcName);
    if (mRegisteredHandlers.containsKey(rpcName)) {
      return mRegisteredHandlers.get(rpcName)
          .handleRequest(command.substring(rpcName.length() + 1));
    } else {
      return null;
    }
  }
}
