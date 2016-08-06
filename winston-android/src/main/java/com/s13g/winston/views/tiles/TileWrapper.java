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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.s13g.winston.R;

/**
 * Wraps a tile and adds common information, such as indicators for when new data was received and
 * when data could be outdated.
 * <p>
 * Also enables us to show some debug data when required.
 */
public class TileWrapper extends LinearLayout {
  private TextView mTitleView;
  private FrameLayout mPlaceholder;

  public TileWrapper(Context context) {
    super(context);
    initialize();
  }

  public TileWrapper(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public TileWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public TileWrapper(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize();
  }

  private void initialize() {
    inflate(getContext(), R.layout.tile_wrapper, this);
    mTitleView = (TextView) findViewById(R.id.title);
    mPlaceholder = (FrameLayout) findViewById(R.id.placeholder);
    mTitleView.setText("Living room");
    mPlaceholder.addView(new TemperatureTile(getContext()));
  }

//  @Override
//  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//    super.onMeasure(widthMeasureSpec, widthMeasureSpec);
//  }

}
