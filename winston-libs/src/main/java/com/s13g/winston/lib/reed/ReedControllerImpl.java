/*
 * Copyright 2014 Sascha Haeberling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.s13g.winston.lib.reed;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.s13g.winston.lib.core.Pins;

public class ReedControllerImpl implements ReedController {
  private static final Logger LOG = LogManager.getLogger(ReedControllerImpl.class);
  /**
   * Contains the current state of the reed relay. If true, the relay is closed,
   * otherwise it is open.
   */
  private final boolean mRelayClosed[];

  private final HashSet<RelayStateChangedListener> mListeners = new HashSet<>();

  public ReedControllerImpl(int mapping[], GpioController gpioController) {
    LOG.info("Initializing with mapping: " + Arrays.toString(mapping));
    mRelayClosed = new boolean[mapping.length];
    initializePins(mapping, gpioController, (relayNum, closed) -> {
      LOG.debug("Relay " + relayNum + " now " + (closed ? "Closed" : "Open"));
      mRelayClosed[relayNum] = closed;

      synchronized (mListeners) {
        // TODO: Think about creating a separate event delivery thread.
        for (final RelayStateChangedListener listener : mListeners) {
          listener.onRelayStateChanged(relayNum, closed);
        }
      }
    });
    LOG.info("Reed relays initialized");
  }

  @Override
  public boolean isClosed(int num) {
    if (num < 0 || num >= mRelayClosed.length) {
      LOG.warn("Invalid reed relay number: " + num);
      return false;
    }

    return mRelayClosed[num];
  }

  @Override
  public void addListener(RelayStateChangedListener listener) {
    synchronized (mListeners) {
      if (mListeners.contains(listener)) {
        LOG.error("Listener already registered");
        return;
      }
      mListeners.add(listener);
    }
  }

  @Override
  public void removeListener(RelayStateChangedListener listener) {
    synchronized (mListeners) {
      if (!mListeners.contains(listener)) {
        LOG.error("Listener never registered");
        return;
      }
      mListeners.remove(listener);
    }
  }

  private static GpioPinDigitalInput[] initializePins(int mapping[], GpioController gpioController,
      final RelayStateChangedListener listener) {
    final GpioPinDigitalInput[] pins = new GpioPinDigitalInput[mapping.length];
    for (int i = 0; i < mapping.length; ++i) {
      pins[i] = gpioController.provisionDigitalInputPin(Pins.GPIO_PIN[mapping[i]],
          PinPullResistance.PULL_UP);
      pins[i].setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
      final int relayNum = i;
      pins[i].addListener(new GpioPinListenerDigital() {

        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
          listener.onRelayStateChanged(relayNum, event.getState() == PinState.LOW);
        }
      });

      // Tell the listener about the current state before change events are
      // received.
      listener.onRelayStateChanged(relayNum, pins[i].getState() == PinState.LOW);
    }
    return pins;
  }
}
