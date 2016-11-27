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

package com.s13g.winston.controller;

import android.util.Log;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.s13g.winston.async.Provider;
import com.s13g.winston.views.tiles.TileView;

import javax.annotation.Nullable;

/**
 * Basic controller for tiles.
 */
public class BasicTileController<V extends TileView<T>, T> implements TileController {
  private static final String TAG = "BasicTileCtrl";

  private final V mTileView;
  private final Provider<T> mProvider;
  private final Object mLock;
  private ListenableFuture<Boolean> mPreviousRequest;

  public BasicTileController(V tileView, Provider<T> provider) {
    mTileView = Preconditions.checkNotNull(tileView);
    mProvider = Preconditions.checkNotNull(provider);
    mLock = new Object();
    mPreviousRequest = null;
  }

  public ListenableFuture<Boolean> refresh() {
    synchronized (mLock) {
      if (mPreviousRequest != null && !mPreviousRequest.isCancelled()) {
        mPreviousRequest.cancel(true);
      }

      final SettableFuture<Boolean> successFuture = SettableFuture.create();
      Futures.addCallback(mProvider.provide(), new FutureCallback<T>() {
        @Override
        public void onSuccess(@Nullable T result) {
          if (result == null) {
            Log.d(TAG, "Failed to get value (null)");
            successFuture.set(false);
          } else {
            mTileView.setValue(result);
            successFuture.set(true);
          }
        }

        @Override
        public void onFailure(Throwable t) {
          Log.d(TAG, "Failed to get value", t);
          successFuture.set(false);
        }
      });
      mPreviousRequest = successFuture;
      return successFuture;
    }
  }
}
