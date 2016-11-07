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

package com.s13g.winston.lib.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Closes multiple {@link Closeable}s safely, even if some throw an exception when being closed.
 */
@ThreadSafe
public class MultiCloseable implements Closeable {
  private static final Logger LOG = LogManager.getLogger(MultiCloseable.class);
  private final List<Closeable> mCloseables;
  private final Object mLock;
  private boolean mIsClosed;

  public MultiCloseable() {
    mCloseables = new LinkedList<>();
    mLock = new Object();
    mIsClosed = false;
  }

  /**
   * Add new closeables to be closed when {@link #close()} is called.
   */
  public MultiCloseable add(Closeable... closeables) {
    for (Closeable closeable : closeables) {
      add(closeable);
    }
    return this;
  }

  /**
   * Add a new closeable to be closed when {@link #close()} is called.
   */
  private MultiCloseable add(Closeable closeable) {
    synchronized (mLock) {
      if (mIsClosed) {
        LOG.error("Cannot add to closed MultiCloseable.");
      } else {
        mCloseables.add(closeable);
      }
      return this;
    }
  }

  @Override
  public void close() throws IOException {
    synchronized (mLock) {
      if (mIsClosed) {
        LOG.warn("MultiCloseable already closed.");
        return;
      }
      mIsClosed = true;
      boolean exceptionThrown = false;
      for (Closeable closeable : mCloseables) {
        try {
          closeable.close();
        } catch (Exception e) {
          LOG.warn("Closeable threw exception: ", e);
          exceptionThrown = true;
        }
      }
      if (exceptionThrown) {
        throw new IOException("At least one closeable threw an exception. See logs.");
      }
    }
  }
}
