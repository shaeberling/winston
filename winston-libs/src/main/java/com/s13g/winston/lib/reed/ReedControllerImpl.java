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

import com.google.common.flogger.FluentLogger;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.s13g.winston.lib.core.Pins;
import com.s13g.winston.lib.plugin.NodePluginType;

import java.util.Arrays;
import java.util.HashSet;

public class ReedControllerImpl implements ReedController {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  /**
   * Contains the current state of the reed relay. If true, the relay is closed, otherwise it is
   * open.
   */
  private final boolean mRelayClosed[];

  private final HashSet<RelayStateChangedListener> mListeners = new HashSet<>();

  public ReedControllerImpl(int mapping[], GpioController gpioController) {
    log.atInfo().log("Initializing with mapping: " + Arrays.toString(mapping));
    mRelayClosed = new boolean[mapping.length];
    initializePins(mapping, gpioController, (relayNum, closed) -> {
      log.atFine().log("Relay " + relayNum + " now " + (closed ? "Closed" : "Open"));
      mRelayClosed[relayNum] = closed;

      synchronized (mListeners) {
        // TODO: Think about creating a separate event delivery thread.
        for (final RelayStateChangedListener listener : mListeners) {
          listener.onRelayStateChanged(relayNum, closed);
        }
      }
    });
    log.atInfo().log("Reed relays initialized");
  }

  private static GpioPinDigitalInput[] initializePins(int mapping[], GpioController gpioController,
                                                      final RelayStateChangedListener listener) {
    final GpioPinDigitalInput[] pins = new GpioPinDigitalInput[mapping.length];
    for (int i = 0; i < mapping.length; ++i) {
      pins[i] = gpioController.provisionDigitalInputPin(Pins.GPIO_PIN[mapping[i]],
          PinPullResistance.PULL_UP);
      final int relayNum = i;
      pins[i].addListener((GpioPinListenerDigital) event -> {
        listener.onRelayStateChanged(relayNum, event.getState() == PinState.LOW);
      });

      // Tell the listener about the current state before change events are
      // received.
      listener.onRelayStateChanged(relayNum, pins[i].getState() == PinState.LOW);
    }
    return pins;
  }

  @Override
  public boolean isClosed(int num) {
    if (num < 0 || num >= mRelayClosed.length) {
      log.atWarning().log("Invalid reed relay number: %d", num);
      return false;
    }
    return mRelayClosed[num];
  }

  @Override
  public void addListener(RelayStateChangedListener listener) {
    synchronized (mListeners) {
      if (mListeners.contains(listener)) {
        log.atSevere().log("Listener already registered");
        return;
      }
      mListeners.add(listener);
    }
    // Send out initial state to newly registered listener.
    // TODO: Think about creating a separate event delivery thread.
    for (int i = 0; i < mRelayClosed.length; ++i) {
      listener.onRelayStateChanged(i, mRelayClosed[i]);
    }
  }

  @Override
  public void removeListener(RelayStateChangedListener listener) {
    synchronized (mListeners) {
      if (!mListeners.contains(listener)) {
        log.atSevere().log("Listener never registered");
        return;
      }
      mListeners.remove(listener);
    }
  }

  @Override
  public NodePluginType getType() {
    return NodePluginType.REED;
  }
}
