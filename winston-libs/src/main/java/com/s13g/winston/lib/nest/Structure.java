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

package com.s13g.winston.lib.nest;

import com.s13g.winston.lib.nest.data.AwayMode;
import com.s13g.winston.lib.nest.data.StructureData;

import java.util.Optional;

/**
 * A Nest structure that can be queried for the latest data.
 */
public class Structure {
  private final String mId;
  private final NestController mNestController;

  private boolean mLastRefreshSuccess;
  private long mLastRefreshTime;
  private StructureData mLatestData;

  public Structure(String id, NestController nestController) {
    mId = id;
    mNestController = nestController;
  }

  public Structure refresh(long maxAgeMillis) {
    boolean dataExpired = mLastRefreshTime + maxAgeMillis < System.currentTimeMillis();
    if (maxAgeMillis < 0 || !dataExpired) {
      return this;
    }
    mNestController.refresh();
    mLastRefreshSuccess = false;
    for (StructureData data : mNestController.getStructures()) {
      if (data.id.equals(mId)) {
        mLatestData = data;
        mLastRefreshSuccess = true;
        mLastRefreshTime = System.currentTimeMillis();
        break;
      }
    }
    return this;

  }

  public Optional<String> getName() {
    return returnIfRefreshSuccessful(mLatestData.name);
  }

  public Optional<AwayMode> getAwayMode() {
    return returnIfRefreshSuccessful(mLatestData.awayMode);
  }

  private <T> Optional<T> returnIfRefreshSuccessful(T value) {
    if (!mLastRefreshSuccess) {
      return Optional.empty();
    }
    return Optional.of(value);
  }
}
