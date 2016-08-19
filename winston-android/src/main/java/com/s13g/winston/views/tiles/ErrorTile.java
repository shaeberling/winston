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
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * A tile that is displayed if an actual tile cannot be created. E.g. used for unknown types.
 */
public class ErrorTile extends View {
  public ErrorTile(Context context) {
    super(context);
    initialize();
  }

  public ErrorTile(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public ErrorTile(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public ErrorTile(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Make the tile a square, based on width.
    int size = MeasureSpec.getSize(widthMeasureSpec);
    setMeasuredDimension(size, size);
  }

  private void initialize() {
    // TODO: Show a nice error icon.
    this.setBackgroundColor(Color.RED);
  }

}
