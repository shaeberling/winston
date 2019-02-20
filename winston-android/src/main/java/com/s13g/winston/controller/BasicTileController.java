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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.s13g.winston.async.Provider;
import com.s13g.winston.views.tiles.TileView;

import java.util.concurrent.Executor;

import javax.annotation.Nullable;

/**
 * Basic controller for tiles.
 */
abstract class BasicTileController<V extends TileView<T>, T> implements TileController {
  private static final String TAG = "BasicTileCtrl";

  private final V mTileView;
  private final Provider<T> mProvider;
  private final Function<T, ListenableFuture<Boolean>> mMainAction;
  // FIXME: This should be passed in.
  private final Executor postUpdateExecutor;
  private final Object mLock;

  @Nullable
  private T mValue;
  private ListenableFuture<Boolean> mPreviousRequest;

  BasicTileController(V tileView, Provider<T> provider,
                      Function<T, ListenableFuture<Boolean>> mainAction) {
    mTileView = Preconditions.checkNotNull(tileView);
    mProvider = Preconditions.checkNotNull(provider);
    mMainAction = mainAction;
    postUpdateExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
    mLock = new Object();
    mValue = null;
    mPreviousRequest = null;
  }

  @Override
  public ListenableFuture<Boolean> onMainAction() {
    synchronized (mLock) {
      return mMainAction.apply(updateValueOnMainAction(mValue));
    }
  }

  @Override
  public ListenableFuture<Boolean> onRefresh() {
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
            updateValue(result);
            mTileView.setValue(result);
            successFuture.set(true);
          }
        }

        @Override
        public void onFailure(Throwable t) {
          Log.d(TAG, "Failed to get value", t);
          successFuture.set(false);
        }
      }, postUpdateExecutor);
      mPreviousRequest = successFuture;
      return successFuture;
    }
  }

  /** Tells us what the updated value will be on main action. */
  protected abstract T updateValueOnMainAction(T currentValue);

  private void updateValue(T value) {
    synchronized (mLock) {
      mValue = value;
    }
  }
}
