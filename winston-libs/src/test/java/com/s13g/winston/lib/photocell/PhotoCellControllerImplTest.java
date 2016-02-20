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

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.s13g.winston.lib.core.util.concurrent.WinstonScheduledExecutor;
import com.s13g.winston.lib.plugin.NodePluginType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link PhotoCellControllerImpl}.
 */
public class PhotoCellControllerImplTest {
  private GpioController mGpioController;
  private PhotoCellControllerImpl mPhotoCellController;
  private GpioPinDigitalMultipurpose mPin;
  private ExecutorForTesting mExecutor;

  @Before
  public void initialize() {
    mExecutor = new ExecutorForTesting();

    int[] mapping = new int[]{4};
    mGpioController = mock(GpioController.class);
    mPin = mock(GpioPinDigitalMultipurpose.class);
    when(mGpioController.provisionDigitalMultipurposePin(any(Pin.class), any(PinMode.class)))
        .thenReturn(mPin);
    mPhotoCellController =
        new PhotoCellControllerImpl(mapping, mGpioController, mExecutor, 0 /* Don't sleep */);
  }

  @Test
  public void testFail1IllegalGpioPin() {
    try {
      new PhotoCellControllerImpl(new int[]{4242}, mGpioController);
      Assert.fail("GPIO Pin is too high and thus should fail initialization.");
    } catch (IllegalArgumentException expected) {
      // Expected
    }
  }

  @Test
  public void testFailOnMoreThanOneMapping() {
    // Support for more than one photo cell not implemented (yet).
    try {
      new PhotoCellControllerImpl(new int[]{1, 2}, mGpioController);
      Assert.fail("Instantiating mapping with size > 1 should fail.");
    } catch (IllegalArgumentException expected) {
      // Expected
    }
  }

  @Test
  public void testPluginType() {
    assertEquals(NodePluginType.PHOTOCELL, mPhotoCellController.getType());
  }

  @Test
  public void testInitialValue() {
    assertEquals(0, mPhotoCellController.getLightValue());
  }

  @Test
  public void testTiming() throws InterruptedException {
    ArgumentCaptor<GpioPinListenerDigital> listenerCaptor =
        ArgumentCaptor.forClass(GpioPinListenerDigital.class);
    verify(mPin).addListener(listenerCaptor.capture());
    GpioPinListenerDigital listener = listenerCaptor.getValue();
    assertNotNull(listener);

    GpioPinDigitalStateChangeEvent highEvent = mock(GpioPinDigitalStateChangeEvent.class);
    when(highEvent.getState()).thenReturn(PinState.HIGH);

    // Test less and less timings. The less time to discharge the cap, the higher the light value.
    // Note: These are all rough values. These could be flaky, but we're using isAtMost to ensure
    // that a slower running test still succeeds. Using a range would be better, but risks
    // flakiness.
    mExecutor.runCommand();
    Thread.sleep(200);
    listener.handleGpioPinDigitalStateChangeEvent(highEvent);
    mExecutor.runCommand();
    assertThat(mPhotoCellController.getLightValue()).isAtMost(10);

    mExecutor.runCommand();
    Thread.sleep(150);
    listener.handleGpioPinDigitalStateChangeEvent(highEvent);
    mExecutor.runCommand();
    assertThat(mPhotoCellController.getLightValue()).isAtMost(30);

    mExecutor.runCommand();
    Thread.sleep(100);
    listener.handleGpioPinDigitalStateChangeEvent(highEvent);
    mExecutor.runCommand();
    assertThat(mPhotoCellController.getLightValue()).isAtMost(60);

    mExecutor.runCommand();
    Thread.sleep(50);
    listener.handleGpioPinDigitalStateChangeEvent(highEvent);
    mExecutor.runCommand();
    assertThat(mPhotoCellController.getLightValue()).isAtMost(80);

    mExecutor.runCommand();
    Thread.sleep(10);
    listener.handleGpioPinDigitalStateChangeEvent(highEvent);
    mExecutor.runCommand();
    assertThat(mPhotoCellController.getLightValue()).isAtMost(99);

    // Running the command twice without getting a HIGH signal means it's too dark for the sensor
    // to charge the capacitor enough.
    mExecutor.runCommand();
    mExecutor.runCommand();
    assertEquals(0, mPhotoCellController.getLightValue());
  }

  /** Allows us to execute commands at will. */
  private static class ExecutorForTesting implements WinstonScheduledExecutor {
    private Runnable mCommand;

    @Override
    public void scheduleAtFixedRate(Runnable command, long initialDelay, long period,
                                    TimeUnit unit) {
      mCommand = command;
    }

    void runCommand() {
      mCommand.run();
    }
  }
}
