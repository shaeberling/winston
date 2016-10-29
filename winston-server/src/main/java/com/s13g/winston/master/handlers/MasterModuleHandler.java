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

package com.s13g.winston.master.handlers;

import com.s13g.winston.common.RequestHandler;
import com.s13g.winston.common.RequestHandlingException;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.ChannelException;
import com.s13g.winston.master.channel.ChannelValue;
import com.s13g.winston.master.modules.Module;
import com.s13g.winston.master.modules.ModuleRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.http.Status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Handles requests for master modules, such as the 'nest' module.
 */
public class MasterModuleHandler implements RequestHandler {
  private static final Logger LOG = LogManager.getLogger(MasterModuleHandler.class);
  private static final String REQ_PREFIX = "io";

  private final Map<String, Map<String, Channel>> mChannels;

  public MasterModuleHandler(ModuleRegistry moduleRegistry) {
    mChannels = new HashMap<>();
    for (Module module : moduleRegistry.getActiveModules()) {
      if (!mChannels.containsKey(module.getType())) {
        mChannels.put(module.getType(), new HashMap<>());
      }
      for (Channel channel : module.getChannels()) {
        mChannels.get(module.getType()).put(channel.getChannelId(), channel);
      }
    }
  }

  @Override
  public String doHandle(String request) throws RequestHandlingException {
    String[] path = request.split("/");
    if (!REQ_PREFIX.equals(path[0])) {
      throw new RequestHandlingException("Path does not match: '" + request + "'.");
    }

    if (path.length < 2 || isNullOrEmpty(path[0]) || isNullOrEmpty(path[1])) {
      throw new RequestHandlingException("Malformed path: '" + request + "'.");
    }

    String module = path[1];
    if (!mChannels.containsKey(module)) {
      throw new RequestHandlingException("Unknown module: '" + module + "'.");
    }
    Map<String, Channel> channels = mChannels.get(module);

    // Only a module name is given... list all the channels.
    if (path.length == 2) {
      String response = "Channels for this module: ";
      for (String channelId : channels.keySet()) {
        response += "'" + channelId + "' ";
      }
      return response;
    }

    String channelId = path[2];
    if (!channels.containsKey(channelId)) {
      throw new RequestHandlingException("Unknown channel '" + module + "/" + channelId + "'.");
    }
    Channel channel = channels.get(channelId);

    // No arguments given... list the channels.
    if (path.length == 3) {
      List<ChannelValue> values = channel.getValues();
      return "Number of channels: " + values.size();
    }

    ChannelValue channelValue;
    try {
      int channelValueId = Integer.parseInt(path[3]);
      channelValue = channel.getValues().get(channelValueId);
    } catch (NumberFormatException ex) {
      throw new RequestHandlingException(
          "Channel value # not a number: '" + request + "'.", Status.BAD_REQUEST);
    }

    if (channelValue == null) {
      throw new RequestHandlingException(
          "Cannot find channel value: '" + request + "'.", Status.BAD_REQUEST);
    }

    // Read request for the channel.
    if (path.length == 4) {
      try {
        return String.valueOf(channelValue.read());
      } catch (ChannelException e) {
        throw new RequestHandlingException(
            "Channel exception on read: '" + e.getMessage() + "'.", Status.BAD_REQUEST);
      }
    }

    // Write request for the channel.
    if (path.length == 5) {
      try {
        channelValue.writeRaw(path[4]);
        return "OK";
      } catch (ChannelException e) {
        throw new RequestHandlingException(
            "Channel exception on write: '" + e.getMessage() + "'.", Status.BAD_REQUEST);
      }
    }

    throw new RequestHandlingException("Illegal request: '" + request + "'.", Status.BAD_REQUEST);
  }

  @Override
  public boolean canHandle(String request) {
    // Master modules respond to the standard /io/ requests that nodes use.
    return request.startsWith(REQ_PREFIX);
  }
}
