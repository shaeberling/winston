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
import android.widget.Toast;

/**
 * Shows toasts.
 */
public class Toaster {
  public enum Duration {
    LONG(Toast.LENGTH_LONG), SHORT(Toast.LENGTH_SHORT);

    final int value;

    Duration(int value) {
      this.value = value;
    }
  }

  private final Context mContext;


  public Toaster(Context context) {
    mContext = context;
  }

  public void showToast(int resId, Duration duration) {
    Toast.makeText(mContext, resId, duration.value).show();
  }

  public void showToast(String message, Duration duration) {
    Toast.makeText(mContext, message, duration.value).show();
  }

}
