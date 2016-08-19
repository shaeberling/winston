/*
 * Changes to this code made 2016 an onwards by the Winston authors.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Maarten Visscher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of  this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.s13g.winston.lib.tv;


import java.io.IOException;
import java.util.concurrent.Executor;

public class SamsungTvController implements TvController {
  private final SamsungRemote mSamsungRemote;
  private final Executor mExecutor;

  public SamsungTvController(SamsungRemote samsungRemote, Executor executor) {
    mSamsungRemote = samsungRemote;
    mExecutor = executor;
  }

  @Override
  public void switchOff() {
    if (ensureConnected()) {
      try {
        mSamsungRemote.sendKeycode(KeyCode.KEY_POWEROFF);
      } catch (IOException e) {
        System.err.println("Error sending command: " + e.getMessage());
      }
    } else {
      System.err.println("Connection not established.");
    }
  }

  private boolean ensureConnected() {
    if (!mSamsungRemote.isConnected()) {
      System.out.println("Not connected, trying to connect...");
      return mSamsungRemote.connectAndAuthenticate();
    }
    return true;
  }
}