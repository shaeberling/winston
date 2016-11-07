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

package com.s13g.winston.master.channel.instance;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.s13g.winston.RequestHandlers;
import com.s13g.winston.common.RequestHandlingException;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.ChannelException;
import com.s13g.winston.master.channel.ChannelValue;
import com.s13g.winston.proto.Master;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This channel produces a value for each configured group.
 */
public class GroupChannel implements Channel {
  private static final Logger LOG = LogManager.getLogger(GroupChannel.class);
  private final List<Master.Group> mGroups;
  private final RequestHandlers mRequestHandlers;
  private final Executor mExecutor;

  public GroupChannel(List<Master.Group> groups, RequestHandlers requestHandlers) {
    mGroups = groups;
    mRequestHandlers = requestHandlers;
    mExecutor = Executors.newCachedThreadPool();
  }

  @Override
  public String getChannelId() {
    return "group";
  }

  @Override
  public List<ChannelValue> getValues() {
    List<ChannelValue> values = new LinkedList<>();
    for (Master.Group group : mGroups) {
      values.add(new GroupChannelValue(group.getName(), group.getTriggerList()));
    }
    return ImmutableList.copyOf(values);
  }

  private class GroupChannelValue implements ChannelValue<String> {
    private final String mName;
    private final List<Master.GroupTrigger> mTriggers;

    private GroupChannelValue(String name, List<Master.GroupTrigger> triggers) {
      mName = name;
      mTriggers = triggers;
    }

    @Override
    public Mode getType() {
      return Mode.WRITE_ONLY;
    }

    @Override
    public String getName() {
      return mName;
    }

    @Override
    public void writeRaw(String value) throws ChannelException {
      for (Master.GroupTrigger trigger : mTriggers) {
        for (String input : trigger.getInputList()) {
          if (input.equals(value)) {
            for (String action : trigger.getActionList()) {
              try {
                // TODO: Consider doing this asynchronously so reuests can fire in parallel.
                mRequestHandlers.handleRequest(action);
              } catch (RequestHandlingException e) {
                throw new ChannelException("Some request in the group failed '" + mName + "'.");
              }
            }
          }
        }
      }
    }

    @Override
    public void write(String value) throws ChannelException {
      throw new ChannelException("Cannot write to GroupChannelValue");
    }

    @Override
    public String read() throws ChannelException {
      throw new ChannelException("Cannot read from GroupChannelValue");
    }
  }
}
