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

package com.s13g.winston.views.tiles;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.s13g.winston.R;

/**
 * Wraps a tile and adds common information, such as indicators for when new data was received and
 * when data could be outdated.
 * <p>
 * Also enables us to show some debug data when required.
 */
public class TileWrapperView extends LinearLayout {

  public TileWrapperView(Context context) {
    super(context);
    initialize();
  }

  public TileWrapperView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public TileWrapperView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public TileWrapperView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize();
  }

  public void setTileView(String title, View view) {
    TextView titleView = (TextView) findViewById(R.id.title);
    titleView.setText(title);
    ((ViewGroup) findViewById(R.id.placeholder)).addView(view);
  }

  public void setFooterText(final int resId) {
    post(new Runnable() {
      @Override
      public void run() {
        ((TextView) findViewById(R.id.footer)).setText(resId);
      }
    });
  }

  private void initialize() {
    inflate(getContext(), R.layout.tile_wrapper, this);
    LayoutParams layoutParams = new LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1);
    this.setLayoutParams(layoutParams);
  }
}
