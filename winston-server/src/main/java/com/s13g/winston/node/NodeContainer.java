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

package com.s13g.winston.node;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.s13g.winston.lib.core.Provider;
import com.s13g.winston.lib.core.SingletonProvider;
import com.s13g.winston.node.handler.Handler;
import com.s13g.winston.node.plugin.NodePlugin;
import com.s13g.winston.node.plugin.NodePluginCreator;
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
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Container for serving the node HTTP requests from.
 */
@ParametersAreNonnullByDefault
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
  @Nonnull
  public static NodeContainer from(NodeProtos.Config config) {

    final Provider<GpioController> gpioController = SingletonProvider.from
        (GpioFactory::getInstance);
    NodePluginCreator nodePluginCreator = new NodePluginCreator(gpioController.provide());
    List<Handler> activeHandlers = new ArrayList<>();

    // For each configured plugin we instantiate the controller and its handler, if existing.
    // NOTE: The order is important since some plugins might depend on other controllers and
    // therefore need to be instantiated later.
    // ==== GPIO ====
    for (NodeProtos.Config.GpioPlugin gpioPlugin : config.getGpioPluginsList()) {
      NodePlugin plugin = nodePluginCreator.create(gpioPlugin);
      if (plugin.hasHandler()) {
        // Add all active handlers so we can forward HTTP requests to it.
        activeHandlers.add(plugin.handler.get());
      }
    }

    // ==== 1-Wire ====
    for (NodeProtos.Config.OneWirePlugin oneWirePlugin : config.getOnewirePluginsList()) {
      NodePlugin plugin = nodePluginCreator.create(oneWirePlugin);
      if (plugin.hasHandler()) {
        // Add all active handlers so we can forward HTTP requests to it.
        activeHandlers.add(plugin.handler.get());
      }
    }
    return new NodeContainer(config.getDaemonPort(), createHandlerMap(activeHandlers));
  }

  @Nonnull
  private static HashMap<String, Handler> createHandlerMap(List<Handler> handlers) {
    final HashMap<String, Handler> handlerMap = new HashMap<>();
    for (final Handler handler : handlers) {
      String rpcName = handler.getRpcName().name().toLowerCase();
      if (handlerMap.containsKey(rpcName)) {
        // TODO: Consider supporting multiple handlers with the same name.
        throw new RuntimeException("RPC name already registered: " + rpcName);
      }
      handlerMap.put(rpcName, handler);
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
    // Ignore favicon requests.
    if ("/favicon.ico".equals(requestUrl)) {
      resp.setStatus(Status.NOT_FOUND);
      return;
    }
    LOG.info("Request: " + requestUrl);

    Optional<String> returnValue = Optional.empty();
    if (requestUrl.startsWith(IO_PREFIX)) {
      returnValue = handleIoRequest(requestUrl.substring(IO_PREFIX.length()));
    }

    try {
      resp.setStatus(returnValue.isPresent() ? Status.OK : Status.NOT_FOUND);
      resp.getPrintStream().append(returnValue.isPresent() ? returnValue.get() : "");
      resp.close();
    } catch (final IOException e) {
      LOG.warn("Could not deliver response");
    }
    LOG.debug("Request handled");
  }

  /**
   * Handles '/io' requests.
   *
   * @return If the request was handled this returns the return values of the handler, otherwise
   * empty is returned.
   */
  @Nonnull
  private Optional<String> handleIoRequest(String command) {
    final String rpcName = command.substring(0, command.indexOf('/'));
    LOG.info("IO RPC: " + rpcName);
    if (mRegisteredHandlers.containsKey(rpcName)) {
      return Optional.of(mRegisteredHandlers.get(rpcName)
          .handleRequest(command.substring(rpcName.length() + 1)));
    } else {
      return Optional.empty();
    }
  }
}
