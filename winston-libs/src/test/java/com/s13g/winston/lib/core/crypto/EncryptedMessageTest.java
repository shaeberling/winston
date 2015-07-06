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

package com.s13g.winston.lib.core.crypto;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class EncryptedMessageTest {
  @Test
  public void testBasics() {
    byte[] ivBytes = new byte[]{1, 2, 42};
    byte[] messageBytes = new byte[]{2, 4, 6, 8, 10};
    EncryptedMessage message = new EncryptedMessage(ivBytes, messageBytes);

    // These should not be changed.
    assertArrayEquals(ivBytes, message.iv);
    assertArrayEquals(messageBytes, message.message);
  }
}