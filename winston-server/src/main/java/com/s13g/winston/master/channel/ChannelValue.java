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

/**
 * A concrete value of a channel, such as temperature ot a light switch state.
 */
public interface ChannelValue<T> {
  /** The mode of a channel value, defines whether a value can be written or read. */
  enum Mode {
    READ_ONLY, READ_WRITE, WRITE_ONLY
  }

  /** Returns the mode of the value. */
  Mode getMode();

  /** @return The name of the channel, unique within its channel. */
  String getName();

  /** Writes a raw values, e.g. coming from the REST interface. */
  void writeRaw(String value) throws ChannelException;

  /** Writes the value, if it's allowed. */
  void write(T value) throws ChannelException;

  /** Reads the values, if it's allowed. */
  T read() throws ChannelException;
}
