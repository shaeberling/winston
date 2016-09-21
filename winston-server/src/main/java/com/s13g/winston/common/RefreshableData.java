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

package com.s13g.winston.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;


/**
 * A collection that contains data that needs to be refreshed once in a while.
 */
public class RefreshableData<T> {
  private final Supplier<List<T>> mSupplier;
  private final long mTtlMillis;

  private List<T> mData;
  private long mNextRefreshTimeMillis;
  private final Object mLock;

  /**
   * Initializes the data structure.
   *
   * @param supplier supplier that provides the updated data when needed.
   * @param ttlSeconds the time to live for the data
   */
  public RefreshableData(Supplier<List<T>> supplier, long ttlSeconds) {
    mSupplier = supplier;
    mTtlMillis = ttlSeconds * 1000;

    mData = Collections.unmodifiableList(new ArrayList<T>());
    mNextRefreshTimeMillis = 0;
    mLock = new Object();
  }

  /**
   * Gets an element.
   *
   * @param index the index of the element to get.
   * @return The element.
   */
  public T get(int index) {
    return ensureFreshThenReturn(() -> mData.get(index));
  }

  /**
   * @return The size of the collection.
   */
  public int size() {
    return ensureFreshThenReturn(() -> mData.size());
  }

  /**
   * @return Whether the collection is empty.
   */
  public boolean isEmpty() {
    return ensureFreshThenReturn(() -> mData.isEmpty());
  }

  /**
   * Ensures that the data is fresh before executing the given supplier.
   *
   * @param s the supplier to execute on fresh data.
   * @param <K> the return type
   * @return The result of the supplier on fresh data.
   */
  private <K> K ensureFreshThenReturn(Supplier<K> s) {
    synchronized (mLock) {
      if (System.currentTimeMillis() >= mNextRefreshTimeMillis) {
        refresh();
      }
      return s.get();
    }
  }

  /**
   * Refreshes the data unconditionally.
   */
  private void refresh() {
    mData = Collections.unmodifiableList(mSupplier.get());
    mNextRefreshTimeMillis = System.currentTimeMillis() + mTtlMillis;
  }
}
