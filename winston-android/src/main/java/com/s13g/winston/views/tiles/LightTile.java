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
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * A tile view that renders a widget to display and toggle a light.
 */
public class LightTile extends View {
  private static final int BACKGROUND_PAINT = Color.rgb(245, 245, 245);
  private static final int LIGHT_ON_PAINT = Color.rgb(225, 225, 140);
  private static final int LIGHT_OFF_PAINT = Color.rgb(150, 150, 150);
  private static final int STROKE_WIDTH_PX = 20;

  private Rect mArea;
  private int bulbWidthPx;

  private Paint mBackgroundPaint;
  private Paint mLightOnPaint;
  private Paint mLightOffPaint;

  private Point[] mRayPoints;

  public LightTile(Context context) {
    super(context);
    initialize();
  }

  public LightTile(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public LightTile(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public LightTile(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize();
  }

  private void initialize() {
    mArea = new Rect();
    mBackgroundPaint = new Paint();
    mBackgroundPaint.setColor(BACKGROUND_PAINT);
    mLightOnPaint = new Paint();
    mLightOnPaint.setColor(LIGHT_ON_PAINT);
    mLightOnPaint.setStrokeWidth(STROKE_WIDTH_PX);
    mLightOffPaint = new Paint();
    mLightOffPaint.setColor(LIGHT_OFF_PAINT);
    mLightOffPaint.setStrokeWidth(STROKE_WIDTH_PX);

    // Six rays, each needs two points.
    mRayPoints = new Point[6 * 2];
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // Draw a light consisting of  a circle in the middle and rays being emitted.
    canvas.drawCircle(mArea.centerX(), mArea.centerY(), bulbWidthPx, mLightOnPaint);

    for (int i = 0; i < mRayPoints.length; i = i + 2) {
      canvas.drawLine(
          mRayPoints[i].x, mRayPoints[i].y,
          mRayPoints[i + 1].x, mRayPoints[i + 1].y, mLightOnPaint);
    }
  }

  @Override
  protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
    super.onSizeChanged(width, height, oldWidth, oldHeight);
    mArea.set(0, 0, width, height);
    bulbWidthPx = width / 12;
    int bulbRayPaddingPx = width / 10;
    int rayLengthPx = bulbWidthPx * 2;

    Point center = new Point(mArea.centerX(), mArea.centerY());
    mRayPoints[0] = new Point(center.x, center.y - (bulbWidthPx / 2) - bulbRayPaddingPx);
    mRayPoints[1] = new Point(center.x, mRayPoints[0].y - rayLengthPx);

    for (int i = 2; i < mRayPoints.length; i = i + 2) {
      mRayPoints[i] = rotate(mRayPoints[i - 2], center, 60);
      mRayPoints[i + 1] = rotate(mRayPoints[i - 1], center, 60);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Make the tile a square, based on width.
    int size = MeasureSpec.getSize(widthMeasureSpec);
    setMeasuredDimension(size, size);
  }

  /**
   * Rotates a point aorund the given center.
   * <p>
   * TODO: Move this to a common pace and make it efficient through a matrix.
   */
  private static Point rotate(Point point, Point center, int degrees) {
    Point toRotate = new Point(point.x - center.x, point.y - center.y);

    // x' = x * cos f - y * sin f
    // y' = y * cos f + x * sin f

    final double rad = degrees * Math.PI / 180d;
    final double sinRad = Math.sin(rad);
    final double cosRad = Math.cos(rad);

    final int newX = (int) (toRotate.x * cosRad - toRotate.y * sinRad);
    final int newY = (int) (toRotate.y * cosRad + toRotate.x * sinRad);
    return new Point(newX + center.x, newY + center.y);
  }
}
