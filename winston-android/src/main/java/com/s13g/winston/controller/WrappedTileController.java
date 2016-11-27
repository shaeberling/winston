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

import android.view.View;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.s13g.winston.R;
import com.s13g.winston.views.tiles.TileWrapperView;

import javax.annotation.Nullable;

/**
 * Controller interface for all wrapped tiles.
 */
public class WrappedTileController implements TileController {
  private final TileController mTileController;
  private final TileWrapperView mTileWrapper;

  WrappedTileController(TileController tileController, TileWrapperView tileWrapper) {
    mTileController = tileController;
    mTileWrapper = tileWrapper;

    // When the tile is clicked, send the main action to
    tileWrapper.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onMainAction();
      }
    });
  }

  public View getView() {
    return mTileWrapper;
  }

  @Override
  public ListenableFuture<Boolean> onMainAction() {
    return Futures.transformAsync(mTileController.onMainAction(),
        new AsyncFunction<Boolean, Boolean>() {
          @Override
          public ListenableFuture<Boolean> apply(@Nullable Boolean input) throws Exception {
            if (input == null || !input) {
              mTileWrapper.setFooterText(R.string.tile_action_failed);
              return Futures.immediateFuture(false);
            }
            return onRefresh();
          }
        });
  }

  @Override
  public ListenableFuture<Boolean> onRefresh() {
    mTileWrapper.setFooterText(R.string.tile_refreshing);
    ListenableFuture<Boolean> refreshFuture = mTileController.onRefresh();
    Futures.addCallback(refreshFuture, new FutureCallback<Boolean>() {
      @Override
      public void onSuccess(@Nullable Boolean success) {
        if (success == null) {
          mTileWrapper.setFooterText(R.string.tile_refreshing_failed);
        } else {
          mTileWrapper.setFooterText(
              success ? R.string.tile_refreshing_success : R.string.tile_refreshing_failed);
        }
      }

      @Override
      public void onFailure(Throwable t) {
        mTileWrapper.setFooterText(R.string.tile_refreshing_failed);
      }
    });
    return refreshFuture;
  }

  // Todo, add other actions, such as click, double click etc.
}
