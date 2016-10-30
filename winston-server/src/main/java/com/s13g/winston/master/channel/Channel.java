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

package com.s13g.winston.master.channel;

import java.util.List;

/**
 * A channel to a concrete item that has values to read and write, such as a switch or a thermostat.
 */
public interface Channel {
  /** The ID that uniquly identifies this channel within its module. */
  String getChannelId();

  /**
   * Creates and returns all the channel values that belong to this channel.
   */
  List<ChannelValue> getValues();
}
