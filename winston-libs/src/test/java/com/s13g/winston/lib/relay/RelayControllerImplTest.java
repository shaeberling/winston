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

package com.s13g.winston.lib.relay;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.s13g.winston.lib.core.Pins;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RelayControllerImplTest {
  private GpioController mGpioController;
  private RelayControllerImpl mRelayController;

  @Before
  public void initialize() {
    int[] mapping = new int[]{3, 11, 23, 27};
    mGpioController = mock(GpioController.class);
    mRelayController = new RelayControllerImpl(mapping, mGpioController);
  }

  @Test
  public void testFirstSwitchOn() {
    GpioPinDigitalOutput pinIo = mock(GpioPinDigitalOutput.class);
    when(mGpioController.provisionDigitalOutputPin(Pins.GPIO_PIN[23],
        PinState.LOW)).thenReturn(pinIo);

    mRelayController.switchRelay(2, true);

    // Make sure the right PIN is provisioned and the shutdown options are set.
    verify(mGpioController, times(1)).provisionDigitalOutputPin(Pins.GPIO_PIN[23], PinState.LOW);
    verify(pinIo, times(1)).setShutdownOptions(true, PinState.HIGH, PinPullResistance.OFF);

    // Pin state is changes through provisioning.
    verify(pinIo, never()).setState(any(PinState.class));
  }

  @Test
  public void testSecondAction() {
    GpioPinDigitalOutput pinIo = mock(GpioPinDigitalOutput.class);
    when(mGpioController.provisionDigitalOutputPin(Pins.GPIO_PIN[23],
        PinState.LOW)).thenReturn(pinIo);

    mRelayController.switchRelay(2, true);
    mRelayController.switchRelay(2, false);
    mRelayController.switchRelay(2, true);
    mRelayController.switchRelay(2, false);

    // The first provisioning will set it high, so we only expect setState with LOW to be called
    // once. NOTE: HIGH is active, LOW is de-active.
    verify(pinIo, times(2)).setState(PinState.HIGH);
    verify(pinIo, times(1)).setState(PinState.LOW);
  }

  @Test
  public void testNoActionOnInvalidPinNum() {
    mRelayController.switchRelay(-4, true);
    mRelayController.switchRelay(4, true);
    mRelayController.switchRelay(5, true);
    mRelayController.switchRelay(40, true);
    mRelayController.switchRelay(424242, true);

    verify(mGpioController, never()).provisionDigitalOutputPin(any(Pin.class), any(PinState.class));
  }


  @Test
  public void testDoNotProvisionOnDisable() {
    mRelayController.switchRelay(0, false);
    mRelayController.switchRelay(1, false);
    mRelayController.switchRelay(2, false);
    mRelayController.switchRelay(3, false);

    // No PIN should be provisioned if we're switching off relays.
    verify(mGpioController, never()).provisionDigitalOutputPin(any(Pin.class), any(PinState.class));
  }

  @Test
  public void testPinProvisionedOnce() {
    GpioPinDigitalOutput pinIo = mock(GpioPinDigitalOutput.class);
    when(mGpioController.provisionDigitalOutputPin(Pins.GPIO_PIN[3],
        PinState.LOW)).thenReturn(pinIo);

    mRelayController.switchRelay(0, true);
    mRelayController.switchRelay(0, false);
    mRelayController.switchRelay(0, true);
    mRelayController.switchRelay(0, false);

    // Pin should only be provisioned once.
    verify(mGpioController, times(1)).provisionDigitalOutputPin(eq(Pins.GPIO_PIN[3]),
        any(PinState.class));

    // No other pin should be provisioned
    verify(mGpioController, never()).provisionDigitalOutputPin(Pins.GPIO_PIN[11], PinState.LOW);
    verify(mGpioController, never()).provisionDigitalOutputPin(Pins.GPIO_PIN[23], PinState.LOW);
    verify(mGpioController, never()).provisionDigitalOutputPin(Pins.GPIO_PIN[27], PinState.LOW);

  }

  @Test
  public void testDoNotProvisionPinStateHigh() {
    GpioPinDigitalOutput pinIo = mock(GpioPinDigitalOutput.class);
    when(mGpioController.provisionDigitalOutputPin(any(), any(PinState.class))).thenReturn(pinIo);

    mRelayController.switchRelay(0, true);
    mRelayController.switchRelay(0, false);
    mRelayController.switchRelay(0, true);
    mRelayController.switchRelay(0, false);

    // Never should be a pin be provisioned HIGH.
    verify(mGpioController, never()).provisionDigitalOutputPin(any(), eq(PinState.HIGH));
  }

  @Test
  public void testClickRelayOnce() {
    mRelayController = new RelayControllerImpl(new int[]{3}, mGpioController, 0 /*clickDelay*/);
    GpioPinDigitalOutput pinIo = mock(GpioPinDigitalOutput.class);
    when(mGpioController.provisionDigitalOutputPin(Pins.GPIO_PIN[3],
        PinState.LOW)).thenReturn(pinIo);

    mRelayController.clickRelay(0);
    verify(mGpioController, times(1)).provisionDigitalOutputPin(Pins.GPIO_PIN[3], PinState.LOW);
    verify(pinIo, times(1)).setState(PinState.HIGH);
  }

  @Test
  public void testClickRelayThreeTimes() {
    mRelayController = new RelayControllerImpl(new int[]{3}, mGpioController, 0 /*clickDelay*/);
    GpioPinDigitalOutput pinIo = mock(GpioPinDigitalOutput.class);
    when(mGpioController.provisionDigitalOutputPin(Pins.GPIO_PIN[3],
        PinState.LOW)).thenReturn(pinIo);

    mRelayController.clickRelay(0);
    mRelayController.clickRelay(0);
    mRelayController.clickRelay(0);

    verify(mGpioController).provisionDigitalOutputPin(Pins.GPIO_PIN[3], PinState.LOW);
    verify(pinIo, times(3)).setState(PinState.HIGH);
    verify(pinIo, times(2)).setState(PinState.LOW);
  }

  @Test
  public void testClickAlreadyOnRelayNoOp() {
    mRelayController = new RelayControllerImpl(new int[]{3}, mGpioController, 0 /*clickDelay*/);
    GpioPinDigitalOutput pinIo = mock(GpioPinDigitalOutput.class);
    when(mGpioController.provisionDigitalOutputPin(Pins.GPIO_PIN[3],
        PinState.LOW)).thenReturn(pinIo);

    mRelayController.switchRelay(0, true);
    when(pinIo.isLow()).thenReturn(true);
    mRelayController.clickRelay(0);
    mRelayController.clickRelay(0);
    mRelayController.clickRelay(0);

    // Should never we called since clickRelay is a no-op and switchRelay will only provision the
    // pin.
    verify(pinIo, never()).setState(any(PinState.class));
  }

  @Test
  public void testInterruptionDuringClickWait() {
    mRelayController = new RelayControllerImpl(new int[]{3}, mGpioController, 0 /*clickDelay*/);
    GpioPinDigitalOutput pinIo = mock(GpioPinDigitalOutput.class);
    when(mGpioController.provisionDigitalOutputPin(Pins.GPIO_PIN[3],
        PinState.LOW)).thenReturn(pinIo);

    Thread.currentThread().interrupt();
    mRelayController.clickRelay(0);
    verify(mGpioController, times(1)).provisionDigitalOutputPin(Pins.GPIO_PIN[3], PinState.LOW);
    // Never called since thread was interrupted.
    verify(pinIo, never()).setState(PinState.HIGH);
  }
}