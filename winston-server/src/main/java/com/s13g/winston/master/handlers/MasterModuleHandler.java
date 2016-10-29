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

import com.google.common.base.Joiner;
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
import java.util.Map;

/**
 * Handles requests for master modules, such as the 'nest' module.
 */
public class MasterModuleHandler implements RequestHandler {
  private static final String REQ_PREFIX = "io";

  /** module -> channel -> channelValue. */
  private final Map<String, Map<String, Map<String, ChannelValue>>> mChannels;

  public MasterModuleHandler(ModuleRegistry moduleRegistry) {
    mChannels = new HashMap<>();
    for (Module module : moduleRegistry.getActiveModules()) {
      if (!mChannels.containsKey(module.getType())) {
        mChannels.put(module.getType(), new HashMap<>());
      }
      Map<String, Map<String, ChannelValue>> moduleMap = mChannels.get(module.getType());
      for (Channel channel : module.getChannels()) {
        if (!moduleMap.containsKey(channel.getChannelId())) {
          moduleMap.put(channel.getChannelId(), new HashMap<>());
        }
        Map<String, ChannelValue> channelMap = moduleMap.get(channel.getChannelId());
        for (ChannelValue channelValue : channel.getValues()) {
          channelMap.put(channelValue.getName(), channelValue);
        }
      }
    }
  }

  @Override
  public String doHandle(String request) throws RequestHandlingException {
    String[] path = request.split("/");
    if (!REQ_PREFIX.equals(path[0])) {
      throw new RequestHandlingException("Path does not match: '" + request + "'.");
    }

    // No arguments given, let's list the active modules.
    if (path.length == 1) {
      return Joiner.on(',').join(mChannels.keySet());
    }

    String module = path[1];
    if (!mChannels.containsKey(module)) {
      throw new RequestHandlingException("Unknown module: '" + module + "'.");
    }
    Map<String, Map<String, ChannelValue>> channels = mChannels.get(module);

    // Only a module name is given, so list all the channel names.
    if (path.length == 2) {
      return Joiner.on(',').join(channels.keySet());
    }

    String channelId = path[2];
    if (!channels.containsKey(channelId)) {
      throw new RequestHandlingException("Unknown channel '" + module + "/" + channelId + "'.");
    }
    Map<String, ChannelValue> channelValues = channels.get(channelId);

    // No arguments given... list the channels.
    if (path.length == 3) {
      return Joiner.on(',').join(channelValues.keySet());
    }

    String channelValueName = path[3];
    if (!channelValues.containsKey(channelValueName)) {
      throw new RequestHandlingException(
          "Unknown channel value name in request '" + request + "'.", Status.BAD_REQUEST);
    }
    ChannelValue channelValue = channelValues.get(channelValueName);

    if (channelValue == null) {
      throw new RequestHandlingException(
          "Cannot find channel value: '" + request + "'.", Status.BAD_REQUEST);
    }

    // Read request for the channel.
    if (path.length == 4) {
      try {
        if (channelValue.getType() == ChannelValue.Mode.WRITE_ONLY) {
          throw new RequestHandlingException(
              "Cannot read from write-only channel: '" + request + "'.", Status.BAD_REQUEST);
        }
        return String.valueOf(channelValue.read());
      } catch (ChannelException e) {
        throw new RequestHandlingException(
            "Channel exception on read: '" + e.getMessage() + "'.", Status.BAD_REQUEST);
      }
    }

    // Write request for the channel.
    if (path.length == 5) {
      try {
        if (channelValue.getType() == ChannelValue.Mode.READ_ONLY) {
          throw new RequestHandlingException(
              "Cannot write to read-only channel: '" + request + "'.", Status.BAD_REQUEST);
        }
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
