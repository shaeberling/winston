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

import com.google.common.flogger.FluentLogger;
import com.google.protobuf.TextFormat;
import com.s13g.winston.common.RequestHandler;
import com.s13g.winston.common.RequestHandlingException;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.ChannelValue;
import com.s13g.winston.master.modules.Module;
import com.s13g.winston.proto.ForClients.ChannelData;



import org.simpleframework.http.Status;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Collection;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Handles requests to serve channel data information to clients.
 */
public class ChannelDataHandler implements RequestHandler {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  private static final String REQ_PREFIX = "channeldata";
  private static final String REQ_PREFIX_TEXT = "channeldatatext";
  private final ChannelData mChannelData;

  public ChannelDataHandler(Collection<Module> modules) {
    ChannelData.Builder builder = ChannelData.newBuilder();
    for (Module module : modules) {
      for (Channel channel : module.getChannels()) {
        ChannelData.Channel.Builder channelBuilder = builder.addChannelBuilder();
        channelBuilder.setId(channel.getChannelId());
        channelBuilder.setModuleType(module.getType());
        channelBuilder.setType(channel.getType().name());
        for (ChannelValue channelValue : channel.getValues()) {
          ChannelData.Channel.ChannelValue.Builder valueBuilder =
              channelBuilder.addValueBuilder();
          valueBuilder.setId(channelValue.getName());
          valueBuilder.setMode(channelValue.getMode().name());
        }
      }
    }
    mChannelData = builder.build();
  }

  @Override
  public void doHandle(String request, OutputStream response) throws RequestHandlingException {
    String[] requestParts = request.split("/");
    if (requestParts[0].equals(REQ_PREFIX_TEXT)) {
      try (OutputStreamWriter writer = new OutputStreamWriter(response)) {
        writer.append(dataToText());
        return;
      } catch (IOException e) {
        log.atSevere().log("Cannot write response", e);
        throw new RequestHandlingException("Cannot write response.", Status.INTERNAL_SERVER_ERROR);
      }
    }
    try {
      mChannelData.writeTo(response);
      response.close();
    } catch (IOException e) {
      log.atSevere().log("Cannot write response", e);
      throw new RequestHandlingException("Cannot write response.", Status.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public boolean canHandle(String request) {
    String[] requestParts = request.split("/");
    return !isNullOrEmpty(request) &&
        (REQ_PREFIX.equals(requestParts[0]) || REQ_PREFIX_TEXT.equals(requestParts[0]));
  }

  private String dataToText() throws RequestHandlingException {
    StringWriter result = new StringWriter();
    try {
      TextFormat.print(mChannelData, result);
      return result.toString();
    } catch (IOException e) {
      log.atSevere().log("Cannot turn protocol buffer into text.", e);
      throw new RequestHandlingException("Cannot produce output", Status.INTERNAL_SERVER_ERROR);
    }
  }
}
