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

package com.s13g.winston.master.modules.instance;

import com.google.common.collect.ImmutableList;
import com.s13g.winston.RequestHandlers;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.instance.GroupChannel;
import com.s13g.winston.master.modules.Module;
import com.s13g.winston.master.modules.ModuleParameters;
import com.s13g.winston.proto.Master;

import java.util.List;

/**
 * The group module is a special module that exposes channels based on groups that have been
 * set-up through the configuration. It fires certain actions on other modules when invoked.
 */
public class GroupModule implements Module {
  private final List<Master.Group> mGroups;
  private final RequestHandlers mRequestHandlers;

  public GroupModule(List<Master.Group> groups, RequestHandlers requestHandlers) {
    mGroups = groups;
    mRequestHandlers = requestHandlers;
  }

  @Override
  public void initialize(ModuleParameters params) throws ModuleInitException {
    // Since this is a special module, we take the configuration data in through the constructor.
  }

  @Override
  public String getType() {
    return "group";
  }

  @Override
  public List<Channel> getChannels() {
    return ImmutableList.of(new GroupChannel(mGroups, mRequestHandlers));
  }
}
