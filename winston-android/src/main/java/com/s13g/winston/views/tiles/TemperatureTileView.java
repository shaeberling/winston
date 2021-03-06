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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.s13g.winston.shared.data.Temperature;

/**
 * A tile view which can display temperature.
 */
public class TemperatureTileView extends View implements TileView<Temperature> {
  private static final float RELATIVE_DRAWING_OFFSET = 0.1f;
  private static final int TEXT_COLOR = Color.BLACK;
  private static final float TEXT_SIZE_FRACTION = 6f;
  private static final float RELATIVE_TEXT_OFFSET = 0.1f + RELATIVE_DRAWING_OFFSET;
  private static final Typeface TYPEFACE = Typeface.create("sans-serif-thin", Typeface.NORMAL);
  private static final float RELATIVE_ARC_INSET = 0.2f;
  private static final float RELATIVE_ARC_THICKNESS = 0.05f;
  // TODO: Use a temperature class to parse and convert between different units.
  private static final String CELSIUS_STRING = "%1$s°C";

  private Config mConfig = new Config(0, 50);
  private float mCurrentTempC = 21.4f;

  private Rect mArea;
  private RectF mArcArea;
  private Paint mTextPaint;
  private Paint mArcPaint;
  private Paint mArcValuePaint;
  private float mTextHeightHalf;

  public TemperatureTileView(Context context) {
    super(context);
    initialize();
  }

  public TemperatureTileView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public TemperatureTileView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public TemperatureTileView(Context context, AttributeSet attrs, int defStyleAttr,
                             int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize();
  }

  @Override
  public void setValue(Temperature temperature) {
    mCurrentTempC = temperature.getRounded(Temperature.Unit.CELSIUS);
    post(new Runnable() {
      @Override
      public void run() {
        invalidate();
      }
    });
  }

  public void setConfig(Config config) {
    mConfig = config;
    invalidate();
  }

  private void initialize() {
    mArea = new Rect();
    mArcArea = new RectF();

    mTextPaint = new Paint();
    mTextPaint.setColor(TEXT_COLOR);
    mTextPaint.setTextAlign(Paint.Align.CENTER);
    mTextPaint.setTypeface(TYPEFACE);

    mArcPaint = new Paint();
    mArcPaint.setColor(Color.GRAY);
    mArcPaint.setStyle(Paint.Style.STROKE);

    mArcValuePaint = new Paint();
    mArcValuePaint.setColor(Color.GREEN);
    mArcValuePaint.setStyle(Paint.Style.STROKE);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    canvas.drawText(String.format(CELSIUS_STRING, mCurrentTempC), mArea.centerX(), mArea.centerY() +
        mTextHeightHalf, mTextPaint);
    canvas.drawArc(mArcArea, 180, 180, false, mArcPaint);

    float tempRange = mConfig.maxTemp - mConfig.minTemp;
    float angle = ((mCurrentTempC - mConfig.minTemp) / tempRange) * 176;
    angle = Math.max(Math.min(angle, 176), 0);
    canvas.drawArc(mArcArea, 182, angle, false, mArcValuePaint);
  }

  @Override
  protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
    super.onSizeChanged(width, height, oldWidth, oldHeight);
    mArea.set(0, 0, width, height);
    mArcArea.set(mArea);
    mArcArea.inset(width * RELATIVE_ARC_INSET, height * RELATIVE_ARC_INSET);
    mArcArea.offset(0, height * RELATIVE_DRAWING_OFFSET);
    mArcPaint.setStrokeWidth(Math.min(width, height) * RELATIVE_ARC_THICKNESS);
    mArcValuePaint.setStrokeWidth(Math.min(width, height) * (RELATIVE_ARC_THICKNESS / 2f));

    mTextPaint.setTextSize(Math.min(width, height) / TEXT_SIZE_FRACTION);
    mTextHeightHalf = (-(mTextPaint.descent() + mTextPaint.ascent()) / 2f) +
        (height * RELATIVE_TEXT_OFFSET);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Make the tile a square, based on width.
    int size = MeasureSpec.getSize(widthMeasureSpec);
    setMeasuredDimension(size, size);
  }

  public static class Config {
    final float minTemp;
    final float maxTemp;


    public Config(float minTemp, float maxTemp) {
      this.minTemp = minTemp;
      this.maxTemp = maxTemp;
    }
  }
}
