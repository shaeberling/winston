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

import java.util.concurrent.TimeUnit;

/**
 * Simplified interface of a {@link java.util.concurrent.ScheduledExecutorService} so we can easily
 * mock it out.
 */
public interface WinstonScheduledExecutor {
  /**
   * See {@link java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(java.lang
   * .Runnable, long, long, java.util.concurrent.TimeUnit)}.
   */
  void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);
}
