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

package com.s13g.winston.lib.core.util.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Default implementation in top of a regular {@link java.util.concurrent.ScheduledExecutorService}.
 * <p/>
 * Note: This is single-threaded and on highest thread priority.
 */
@ParametersAreNonnullByDefault
public class WinstonScheduledExecutorImpl implements WinstonScheduledExecutor {

  private final ScheduledExecutorService mExecutor;

  public WinstonScheduledExecutorImpl(String threadName) {
    mExecutor = Executors.newScheduledThreadPool(1, r -> {
      Thread t = new Thread(r, threadName);
      t.setPriority(Thread.MAX_PRIORITY);
      return t;
    });
  }

  @Override
  public void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
    mExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
  }
}
