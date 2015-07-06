/*
 * Copyright 2015 The Winston Authors
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

package com.s13g.winston.lib.reed;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.s13g.winston.lib.core.Pins;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReedControllerImplTest {
  private final int[] mMapping = new int[]{7, 23};
  private final GpioPinDigitalInput[] mMockPins = new GpioPinDigitalInput[2];
  private GpioController mMockGpioController;
  private ReedControllerImpl mReedController;

  @Before
  public void initialize() {
    mMockPins[0] = mock(GpioPinDigitalInput.class);
    mMockPins[1] = mock(GpioPinDigitalInput.class);
    mMockGpioController = mock(GpioController.class);
    when(mMockGpioController.provisionDigitalInputPin(Pins.GPIO_PIN[7],
        PinPullResistance.PULL_UP)).thenReturn(mMockPins[0]);
    when(mMockGpioController.provisionDigitalInputPin(Pins.GPIO_PIN[23],
        PinPullResistance.PULL_UP)).thenReturn(mMockPins[1]);

    mReedController = new ReedControllerImpl(mMapping, mMockGpioController);
  }

  @Test
  public void testInitialization() {
    verify(mMockGpioController, times(1)).provisionDigitalInputPin(Pins.GPIO_PIN[7],
        PinPullResistance.PULL_UP);
    verify(mMockGpioController, times(1)).provisionDigitalInputPin(Pins.GPIO_PIN[23],
        PinPullResistance.PULL_UP);
    verify(mMockPins[0], times(1)).addListener(any(GpioPinListenerDigital.class));
    verify(mMockPins[1], times(1)).addListener(any(GpioPinListenerDigital.class));
  }

  @Test
  public void testInitialState() {
    assertFalse(mReedController.isClosed(0));
    assertFalse(mReedController.isClosed(1));
  }

  @Test
  public void testIllegalPinsAreClosed() {
    assertFalse(mReedController.isClosed(-42));
    assertFalse(mReedController.isClosed(1023));
  }

  @Test
  public void testAddListeners() {
    ReedController.RelayStateChangedListener mMockListener = mock(ReedController
        .RelayStateChangedListener.class);
    mReedController.addListener(mMockListener);

    verify(mMockListener, times(1)).onRelayStateChanged(0, false);
    verify(mMockListener, times(1)).onRelayStateChanged(1, false);
  }

  @Test
  public void testAddSameListener() {
    ReedController.RelayStateChangedListener mMockListener = mock(ReedController
        .RelayStateChangedListener.class);
    mReedController.addListener(mMockListener);
    // Nothing should happen if the same listener is added multiple times.
    mReedController.addListener(mMockListener);
    mReedController.addListener(mMockListener);
    mReedController.addListener(mMockListener);

    verify(mMockListener, times(1)).onRelayStateChanged(0, false);
    verify(mMockListener, times(1)).onRelayStateChanged(1, false);
  }

  @Test
  public void testRelayStateChanges() {
    ReedController.RelayStateChangedListener mMockListener = mock(ReedController
        .RelayStateChangedListener.class);
    mReedController.addListener(mMockListener);

    ArgumentCaptor<GpioPinListenerDigital> pinOneListenerCaptor = ArgumentCaptor.forClass
        (GpioPinListenerDigital.class);
    ArgumentCaptor<GpioPinListenerDigital> pinTwoListenerCaptor = ArgumentCaptor.forClass
        (GpioPinListenerDigital.class);
    verify(mMockPins[0], times(1)).addListener(pinOneListenerCaptor.capture());
    verify(mMockPins[1], times(1)).addListener(pinTwoListenerCaptor.capture());

    assertFalse(mReedController.isClosed(0));
    assertFalse(mReedController.isClosed(1));

    reset(mMockListener);
    // Fire a fake HIGH event on the first pin.
    pinOneListenerCaptor.getValue().handleGpioPinDigitalStateChangeEvent(
        createMockChangeEvent(PinState.HIGH));
    verify(mMockListener, times(1)).onRelayStateChanged(0, false);
    verify(mMockListener, never()).onRelayStateChanged(eq(1), anyBoolean());
    assertFalse(mReedController.isClosed(0));
    assertFalse(mReedController.isClosed(1));

    reset(mMockListener);
    // Fire a fake LOW event on the first pin.
    pinOneListenerCaptor.getValue().handleGpioPinDigitalStateChangeEvent(
        createMockChangeEvent(PinState.LOW));
    verify(mMockListener, times(1)).onRelayStateChanged(0, true);
    verify(mMockListener, never()).onRelayStateChanged(eq(1), anyBoolean());
    assertTrue(mReedController.isClosed(0));
    assertFalse(mReedController.isClosed(1));

    reset(mMockListener);
    // Fire a fake HIGH event on the second pin.
    pinTwoListenerCaptor.getValue().handleGpioPinDigitalStateChangeEvent(
        createMockChangeEvent(PinState.HIGH));
    verify(mMockListener, never()).onRelayStateChanged(eq(0), anyBoolean());
    verify(mMockListener, times(1)).onRelayStateChanged(1, false);
    assertTrue(mReedController.isClosed(0));
    assertFalse(mReedController.isClosed(1));

    reset(mMockListener);
    // Fire a fake LOW event on the second pin.
    pinTwoListenerCaptor.getValue().handleGpioPinDigitalStateChangeEvent(
        createMockChangeEvent(PinState.LOW));
    verify(mMockListener, never()).onRelayStateChanged(eq(0), anyBoolean());
    verify(mMockListener, times(1)).onRelayStateChanged(1, true);
    assertTrue(mReedController.isClosed(0));
    assertTrue(mReedController.isClosed(1));
  }

  @Test
  public void testRemoveListener() {
    ReedController.RelayStateChangedListener mMockListener = mock(ReedController
        .RelayStateChangedListener.class);
    mReedController.addListener(mMockListener);

    ArgumentCaptor<GpioPinListenerDigital> pinOneListenerCaptor = ArgumentCaptor.forClass
        (GpioPinListenerDigital.class);
    verify(mMockPins[0], times(1)).addListener(pinOneListenerCaptor.capture());

    reset(mMockListener);
    // Fire a fake HIGH event on the first pin.
    pinOneListenerCaptor.getValue().handleGpioPinDigitalStateChangeEvent(
        createMockChangeEvent(PinState.HIGH));
    verify(mMockListener, times(1)).onRelayStateChanged(0, false);

    reset(mMockListener);

    mReedController.removeListener(mMockListener);
    // Fire a fake LOW event on the first pin after removing listener.
    pinOneListenerCaptor.getValue().handleGpioPinDigitalStateChangeEvent(
        createMockChangeEvent(PinState.LOW));
    verify(mMockListener, never()).onRelayStateChanged(eq(0), anyBoolean());
  }

  @Test
  public void testOkToRemoveNonExistentListener() {
    ReedController.RelayStateChangedListener mMockListener = mock(ReedController
        .RelayStateChangedListener.class);
    mReedController.addListener(mMockListener);

    mReedController.removeListener(mock(ReedController.RelayStateChangedListener.class));
    mReedController.removeListener(mMockListener);
    mReedController.removeListener(mock(ReedController.RelayStateChangedListener.class));
    mReedController.removeListener(mMockListener);
    mReedController.removeListener(mock(ReedController.RelayStateChangedListener.class));
  }

  private static GpioPinDigitalStateChangeEvent createMockChangeEvent(PinState state) {
    GpioPinDigitalStateChangeEvent mockChangeEvent = mock(GpioPinDigitalStateChangeEvent.class);
    when(mockChangeEvent.getState()).thenReturn(state);
    return mockChangeEvent;
  }
}