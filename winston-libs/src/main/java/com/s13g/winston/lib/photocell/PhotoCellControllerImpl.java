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

package com.s13g.winston.lib.photocell;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.s13g.winston.lib.core.Pins;
import com.s13g.winston.lib.core.util.concurrent.WinstonScheduledExecutor;
import com.s13g.winston.lib.core.util.concurrent.WinstonScheduledExecutorImpl;
import com.s13g.winston.lib.plugin.NodePluginType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Default implementation for reading the value of a photo cell resistor via a digital GPIO input.
 * <p/>
 * This is achieved by using a capacitor together with the resistor, and measuring the capacitor's
 * discharge time to estimate the resistor value.
 * <p/>
 * Note: This is not precise, but enough for getting a general idea of brightness.
 * <p/>
 * General set-up is described <a href="https://goo.gl/VgfyFj">here</a>.
 */
@ParametersAreNonnullByDefault
public class PhotoCellControllerImpl implements PhotoCellController {
  private static final Logger LOG = LogManager.getLogger(PhotoCellControllerImpl.class);

  private static final boolean DEBUG_LOGGING = false;

  // These values work well for a 2.2nf capacitor and this photo cell: https://goo.gl/8kpZwi.
  /* Time out of the darkest the setup can sense. */
  private static final long MAX_LIGHT_NANOS = 200000000L;
  /* Time out of the brightest scene the setup can sense. */
  private static final long MIN_LIGHT_NANOS = 1700000L;
  private static final long LIGHT_RANGE_NANOS = MAX_LIGHT_NANOS - MIN_LIGHT_NANOS;
  private static final int SLEEP_TIMEOUT_MILLIS = 220;
  private static final int MEASUREMENT_INTERVAL_SECONDS = 5;

  private final int mSleepTimeoutMillis;
  private final GpioPinDigitalMultipurpose mPin;

  private volatile int mLastValuePercent = 0;
  private volatile long mLastStartTimeNanos;

  // These are for debugging only.
  private long mMin = Long.MAX_VALUE;
  private long mMax = Long.MIN_VALUE;

  private volatile int mReadValuePercent;

  public PhotoCellControllerImpl(int[] mapping, GpioController gpioController) {
    this(mapping, gpioController, new WinstonScheduledExecutorImpl("PCellReader"),
        SLEEP_TIMEOUT_MILLIS);
  }

  @VisibleForTesting
  PhotoCellControllerImpl(int[] mapping, GpioController gpioController,
                          WinstonScheduledExecutor executor, int sleepTimeoutMillis) {
    Preconditions.checkArgument(mapping.length == 1,
        "Only one photo cell supported at this time.");
    Preconditions.checkArgument(mapping[0] <= Pins.GPIO_PIN.length, "Illegal GPIO pin.");

    mSleepTimeoutMillis = sleepTimeoutMillis;
    int gpioPin = mapping[0];
    LOG.info("Initializing on GPIO pin: " + gpioPin);
    mPin = gpioController.provisionDigitalMultipurposePin(
        Pins.GPIO_PIN[gpioPin], PinMode.DIGITAL_INPUT);

    mPin.addListener(new GpioPinListenerDigital() {
      @Override
      public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if (event.getState() == PinState.HIGH) {
          long deltaNs = System.nanoTime() - mLastStartTimeNanos;

          if (DEBUG_LOGGING) {
            mMax = Math.max(mMax, deltaNs);
            mMin = Math.min(mMin, deltaNs);
            LOG.info("Value: " + (deltaNs / 1000000) + " ms.  (" + deltaNs + " ns.)");
            LOG.info("Min: " + mMin + "  /  Max: " + mMax);
          }

          // Clamp the value.
          deltaNs = Math.max(Math.min(MAX_LIGHT_NANOS, deltaNs), MIN_LIGHT_NANOS);
          mReadValuePercent = (int) (100 - (deltaNs - MIN_LIGHT_NANOS) / (LIGHT_RANGE_NANOS / 100));
        }
      }
    });
    executor.scheduleAtFixedRate(
        new ReadValuesRunnable(), 0, MEASUREMENT_INTERVAL_SECONDS, TimeUnit.SECONDS);
  }

  @Override
  public int getLightValue() {
    return mLastValuePercent;
  }

  @Override
  public NodePluginType getType() {
    return NodePluginType.PHOTOCELL;
  }

  private void setCurrentValue(int percent) {
    mLastValuePercent = percent;

    if (DEBUG_LOGGING) {
      StringBuilder b = new StringBuilder();
      b.append("[");
      for (int i = 0; i < 100; ++i) {
        if (i <= percent) {
          b.append("#");
        } else {
          b.append(" ");
        }
      }
      b.append("] - " + percent + "%");
      System.out.println(b.toString());
    }
  }


  private class ReadValuesRunnable implements Runnable {

    @Override
    public void run() {
      setCurrentValue(mReadValuePercent);

      mPin.setMode(PinMode.DIGITAL_OUTPUT);
      mPin.setState(PinState.LOW);
      try {
        Thread.sleep(mSleepTimeoutMillis);
      } catch (InterruptedException e) {
        LOG.info("Interrupted.");
        return;
      }
      // If no read-out happened, we want to fall back to zero. It means, that the resistance of
      // the photo cell was so high such that it wasn't able to flip the state of the capacitor
      // at all.
      mReadValuePercent = 0;
      mLastStartTimeNanos = System.nanoTime();
      mPin.setMode(PinMode.DIGITAL_INPUT);
    }
  }
}
