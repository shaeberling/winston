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

package com.s13g.winston.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.s13g.winston.proto.nano.ForClients;
import com.s13g.winston.views.tiles.ErrorTile;
import com.s13g.winston.views.tiles.LightTile;
import com.s13g.winston.views.tiles.TemperatureTile;
import com.s13g.winston.views.tiles.TileWrapper;

import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helps to create and wrap tiles views.
 */
public class TiledViewCreator {
  private static final Logger LOG = Logger.getLogger("TiledViewCreator");
  private final ViewGroup mTileContainer;
  private final Context mContext;

  public TiledViewCreator(ViewGroup tileContainer) {
    mTileContainer = checkNotNull(tileContainer);
    mContext = checkNotNull(mTileContainer.getContext());
  }

  public void addTiles(ForClients.SystemData.IoChannel[] channels) {
    ViewGroup newRow = null;
    for (int i = 0; i < channels.length; ++i) {
      if (i % 2 == 0) {
        newRow = createRow();
        mTileContainer.addView(newRow);
      }
      ForClients.SystemData.IoChannel channel = channels[i];
      LOG.info("Adding view for channel: " + channel.id);
      newRow.addView(wrapTile(createTile(channels[i]), channels[i].name));
    }
    mTileContainer.invalidate();
    mTileContainer.requestLayout();
  }

  private View createTile(ForClients.SystemData.IoChannel channel) {
    switch (channel.type) {
      case "temperature":
        return new TemperatureTile(mContext);
      case "switch/light":
        return new LightTile(mContext);
      default:
        return new ErrorTile(mContext);
    }
  }

  private ViewGroup wrapTile(View view, String title) {
    TileWrapper tileWrapper = new TileWrapper(mContext);
    tileWrapper.setTileView(title, view);
    return tileWrapper;
  }

  private ViewGroup createRow() {
    LinearLayout newRow = new LinearLayout(mContext);
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    newRow.setOrientation(LinearLayout.HORIZONTAL);
    newRow.setLayoutParams(layoutParams);
    return newRow;
  }

}
