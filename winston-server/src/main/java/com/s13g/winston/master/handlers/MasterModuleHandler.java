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
import com.google.common.flogger.FluentLogger;
import com.s13g.winston.common.RequestHandler;
import com.s13g.winston.common.RequestHandlingException;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.ChannelException;
import com.s13g.winston.master.channel.ChannelValue;
import com.s13g.winston.master.modules.Module;



import org.simpleframework.http.Status;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Handles requests for master modules, such as the 'nest' module.
 */
public class MasterModuleHandler implements RequestHandler {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  private static final String REQ_PREFIX = "io";

  /** module -> channel -> channelValue. */
  private final Collection<Module> mModules;

  public MasterModuleHandler(Collection<Module> modules) {
    mModules = modules;
  }

  @Override
  public void doHandle(String request, OutputStream response) throws RequestHandlingException {
    try (OutputStreamWriter writer = new OutputStreamWriter(response)) {
      writer.write(handle(request));
    } catch (IOException e) {
      log.atSevere().log("Cannot write response", e);
      throw new RequestHandlingException("Cannot write response.", Status.INTERNAL_SERVER_ERROR);
    }
  }

  private String handle(String request) throws RequestHandlingException {
    String[] path = request.split("/");
    if (!REQ_PREFIX.equals(path[0])) {
      throw new RequestHandlingException("Path does not match: '" + request + "'.");
    }

    Map<String, Module> modules = mModules.stream().collect(
        Collectors.toMap(Module::getType, Function.identity()));

    // No arguments given, let's list the active modules.
    if (path.length == 1) {
      return Joiner.on(',').join(modules.keySet());
    }

    String moduleType = path[1];
    if (!modules.containsKey(moduleType)) {
      throw new RequestHandlingException("Unknown module: '" + moduleType + "'.");
    }

    Map<String, Channel> channels = modules.get(moduleType).getChannels().stream().collect(
        Collectors.toMap(Channel::getChannelId, Function.identity()));

    // Only a module name is given, so list all the channel names.
    if (path.length == 2) {
      return Joiner.on(',').join(channels.keySet());
    }

    String channelId = path[2];
    if (!channels.containsKey(channelId)) {
      throw new RequestHandlingException("Unknown channel '" + moduleType + "/" + channelId + "'.");
    }

    Map<String, ChannelValue> channelValues = channels.get(channelId).getValues().stream().collect(
        Collectors.toMap(ChannelValue::getName, Function.identity()));

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
        if (channelValue.getMode() == ChannelValue.Mode.WRITE_ONLY) {
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
        if (channelValue.getMode() == ChannelValue.Mode.READ_ONLY) {
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
    return !isNullOrEmpty(request) && request.startsWith(REQ_PREFIX);
  }

}
