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

import com.s13g.winston.lib.plugin.NodeController;

public interface ReedController extends NodeController {
  /**
   * Returns whether the reed sensor with the given number is currently closed.
   */
  boolean isClosed(int num);

  /**
   * Adds the given listener, if it is not already added.
   */
  void addListener(RelayStateChangedListener listener);

  /**
   * Removes the given listener, if it was previously added.
   */
  void removeListener(RelayStateChangedListener listener);

  /**
   * Classes implementing this interface can be informed when the state of a
   * relay changes.
   */
  interface RelayStateChangedListener {
    /**
     * Called when the state of a relay changes.
     *
     * @param relayNum the number of the relay.
     * @param closed   whether the relay is now closed. If false, the relay is open.
     */
    void onRelayStateChanged(int relayNum, boolean closed);
  }
}
