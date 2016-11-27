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
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.s13g.winston.controller.ChannelTileCreator;
import com.s13g.winston.controller.WrappedTileController;
import com.s13g.winston.proto.nano.ForClients.ChannelData;
import com.s13g.winston.shared.ChannelType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helps to create and wrap tiles views.
 */
public class TiledViewCreator {
  private static final Logger LOG = Logger.getLogger("TiledViewCreator");

  private final ViewGroup mTileContainer;
  private final Context mContext;
  private final List<WrappedTileController> mControllers;
  private final Map<ChannelType, ChannelTileCreator> mCreators;

  public TiledViewCreator(ViewGroup tileContainer,
                          Map<ChannelType, ChannelTileCreator> tileCreators) {
    mTileContainer = checkNotNull(tileContainer);
    mContext = checkNotNull(mTileContainer.getContext());
    mControllers = new LinkedList<>();
    mCreators = tileCreators;
  }

  /**
   * Add tile data. Call this on the main thread.
   *
   * @param data tile data.
   */
  public void addTiles(ChannelData data) {
    LOG.info("Adding tiles: " + data.channel.length);

    ViewGroup newRow = null;
    int c = 0;
    for (int i = 0; i < data.channel.length; ++i) {
      ChannelData.Channel channel = data.channel[i];
      LOG.info("Adding views for channel: " + channel.id);
      List<WrappedTileController> tiles = createWrappedTiles(channel);
      for (WrappedTileController tile : tiles) {
        if (c++ % 2 == 0) {
          newRow = createRow();
          mTileContainer.addView(newRow);
        }
        newRow.addView(tile.getView());
      }
      mControllers.addAll(tiles);
    }
    mTileContainer.invalidate();
    mTileContainer.requestLayout();
  }

  public void refreshAll() {
    for (WrappedTileController controller : mControllers) {
      controller.onRefresh();
      // TODO: Use the future to change some global UI maybe.
    }
  }

  private List<WrappedTileController> createWrappedTiles(ChannelData.Channel channel) {
    ChannelType type = ChannelType.valueOf(channel.type);
    if (mCreators.containsKey(type)) {
      return mCreators.get(type).createWrappedTiles(channel);
    } else {
      LOG.log(Level.WARNING, "Not supported channel type: " + type);
    }
    return new LinkedList<>();

//        case WINSTON_POWERBOX:
//        case WEMO_SWITCH:
//          return Lists.newArrayList((View) wrapTile(new LightTile(mContext), channelName));
//        default:
//          return Lists.newArrayList((View) wrapTile(new ErrorTile(mContext), channelName));
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
