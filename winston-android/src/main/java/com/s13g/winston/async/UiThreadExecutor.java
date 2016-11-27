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

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 * An executor that runs everything on the main/UI thread.
 */
public class UiThreadExecutor implements Executor {
  private final Handler mMainHandler;

  public UiThreadExecutor() {
    mMainHandler = new Handler(Looper.getMainLooper());
  }

  @Override
  public void execute(@NonNull Runnable runnable) {
    mMainHandler.post(runnable);
  }
}
