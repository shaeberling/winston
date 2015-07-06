/*
 * Copyright 2015 The Winston Authors
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

package com.s13g.winston.lib.core;

/**
 * A provider that makes sure the given <T> is only ever provided once.
 */
public class SingletonProvider<T> implements Provider<T> {

  private final Provider<T> mProvider;
  private T mCached;

  private SingletonProvider(Provider<T> provider) {
    mProvider = provider;
  }

  public static <T> SingletonProvider<T> from(Provider<T> provider) {
    return new SingletonProvider<T>(provider);
  }

  @Override
  public T provide() {
    if (mCached == null) {
      mCached = mProvider.provide();
    }
    return mCached;
  }

  public boolean isCached() {
    return mCached != null;
  }
}
