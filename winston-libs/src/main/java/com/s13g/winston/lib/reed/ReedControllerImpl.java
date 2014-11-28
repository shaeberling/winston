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
  /**
   * Classes implementing this interface can be informed when the state of a
   * relay changes.
   */
  private static interface RelayStateChangedListener {
    /**
     * Called when the state of a relay changes.
     *
     * @param relayNum
     *          the number of the relay.
     * @param closed
     *          whether the relay is now closed. If false, the relay is open.
     */
    void onRelayStateChanged(int relayNum, boolean closed);
  }

  private static final Logger LOG = LogManager.getLogger(ReedControllerImpl.class);
  /**
   * Contains the current state of the reed relay. If true, the relay is closed,
   * otherwise it is open.
   */
  private final boolean mRelayClosed[];

  public ReedControllerImpl(int mapping[], GpioController gpioController) {
    mRelayClosed = new boolean[mapping.length];
    initializePins(mapping, gpioController, (relayNum, closed) -> {
      LOG.debug("Relay " + relayNum + " now " + (closed ? "Closed" : "Open"));
      mRelayClosed[relayNum] = closed;
    });
    LOG.info("Reed relays initialized");
  }

  @Override
  public boolean isClosed(int num) {
    return mRelayClosed[num];
  }

  private static GpioPinDigitalInput[] initializePins(int mapping[], GpioController gpioController,
      final RelayStateChangedListener listener) {
    final GpioPinDigitalInput[] pins = new GpioPinDigitalInput[mapping.length];
    for (int i = 0; i < mapping.length; ++i) {
      pins[i] = gpioController.provisionDigitalInputPin(Pins.GPIO_PIN[mapping[i]],
          PinPullResistance.PULL_UP);
      final int relayNum = i;
      pins[i].addListener(new GpioPinListenerDigital() {

        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
          listener.onRelayStateChanged(relayNum, event.getState() == PinState.LOW);
        }
      });
    }
    return pins;
  }
}
