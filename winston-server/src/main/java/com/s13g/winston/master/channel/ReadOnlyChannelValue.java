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
 * A channel value that can be used for read-only channels.
 */
public class ReadOnlyChannelValue<T> implements ChannelValue<T> {
  private final String mName;
  private final ChannelReadAction<T> mReadAction;
  private final String mWriteErrorMsg;

  public ReadOnlyChannelValue(String name, ChannelReadAction<T> readAction) {
    this(name, readAction, "Cannot write to this channel '" + name + "'.");
  }

  public ReadOnlyChannelValue(String name, ChannelReadAction<T> readAction, String writeErrorMsg) {
    mName = name;
    mReadAction = readAction;
    mWriteErrorMsg = writeErrorMsg;
  }

  @Override
  public Mode getMode() {
    return Mode.READ_ONLY;
  }

  @Override
  public String getName() {
    return mName;
  }

  @Override
  public void writeRaw(String value) throws ChannelException {
    throw new ChannelException(mWriteErrorMsg);
  }

  @Override
  public void write(T value) throws ChannelException {
    throw new ChannelException(mWriteErrorMsg);
  }

  @Override
  public T read() throws ChannelException {
    return mReadAction.read();
  }

  /**
   * Classes implementing this interface will read a channel value and report and error through
   * a ChannelException.
   *
   * @param <T> the type of value to be read.
   */
  public interface ChannelReadAction<T> {
    T read() throws ChannelException;
  }
}
