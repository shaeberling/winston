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
import com.s13g.winston.common.ActionFailedException;
import com.s13g.winston.lib.wemo.WemoController;
import com.s13g.winston.lib.wemo.WemoSwitch;
import com.s13g.winston.master.ModuleContext;
import com.s13g.winston.master.channel.Channel;
import com.s13g.winston.master.channel.instance.WemoSwitchChannel;
import com.s13g.winston.master.modules.Module;
import com.s13g.winston.master.modules.ModuleCreationException;
import com.s13g.winston.master.modules.ModuleCreator;
import com.s13g.winston.master.modules.ModuleParameters;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Module handling interaction with Belkin's Wemo service.
 */
public class WemoModule implements Module {
  private static final String MODULE_TYPE = "wemo";
  private static final String PARAM_SWITCH_IP = "switch-ip";

  private final String mType;
  private final WemoController mController;

  private List<Channel> mChannels = ImmutableList.of();

  private WemoModule(String type, WemoController controller) {
    mType = type;
    mController = controller;
  }

  @Override
  public void initialize(ModuleParameters params) throws ModuleInitException {
    Optional<List<String>> switchIps = params.get(PARAM_SWITCH_IP);
    if (!switchIps.isPresent()) {
      throw new ModuleInitException("No switch-ip values configured");
    }

    List<Channel> channels = new LinkedList<>();
    for (String switchIp : switchIps.get()) {
      Optional<WemoSwitch> wemoSwitch = mController.querySwitch(switchIp);
      if (!wemoSwitch.isPresent()) {
        throw new ModuleInitException("Cannot find Wemo Switch with IP: " + switchIp);
      }

      // Use the friendly name as he ID for now. We could als choose to use the serial number or
      // IP, as long as it uniquely defines the switch.
      channels.add(new WemoSwitchChannel(
          wemoSwitch.get().getFriendlyName(),
          wemoSwitch.get()));
    }
    mChannels = ImmutableList.copyOf(channels);
  }

  @Override
  public String getType() {
    return mType;
  }

  @Override
  public List<Channel> getChannels() {
    return mChannels;
  }

  public static class Creator implements ModuleCreator<WemoModule> {

    @Override
    public String getType() {
      return MODULE_TYPE;
    }

    @Override
    public WemoModule create(ModuleContext context,
                             ModuleParameters parameters) throws ModuleCreationException {
      WemoModule module = new WemoModule(getType(), context.getWemoController());
      try {
        module.initialize(parameters);
      } catch (ModuleInitException e) {
        throw ModuleCreationException.create("Cannot initialize module", e);
      }
      return module;
    }
  }
}
