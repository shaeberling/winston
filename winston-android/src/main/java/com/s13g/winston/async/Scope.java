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

package com.s13g.winston.async;

import java.util.HashSet;
import java.util.Set;

/**
 * A scope contains closeable objects that will be closed when the scope itself is closed.
 */
public class Scope implements AutoCloseable {
  private final Set<AutoCloseable> mCloseables;
  private boolean mClosed;
  private final Object mLock;

  public Scope() {
    mCloseables = new HashSet<>();
    mClosed = false;
    mLock = new Object();
  }

  @Override
  public void close() throws Exception {
    synchronized (mLock) {
      mClosed = true;
      for (AutoCloseable closeable : mCloseables) {
        closeable.close();
      }
    }
  }

  /**
   * Adds te closeable to the list of items being closes then the scope is being closed
   *
   * @param closeable the closeable to add to the scope.
   * @return For fluent programming, returns the closeable itself.
   */
  public <T extends AutoCloseable> T add(T closeable) {
    synchronized (mLock) {
      if (mClosed) {
        throw new IllegalStateException("Cannot add to a closed scope.");
      }
      mCloseables.add(closeable);
    }
    return closeable;
  }
}
