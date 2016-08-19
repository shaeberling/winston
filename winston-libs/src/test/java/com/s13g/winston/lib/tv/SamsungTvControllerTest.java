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

package com.s13g.winston.lib.tv;

import com.google.common.util.concurrent.MoreExecutors;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SamsungTvController}
 */
public class SamsungTvControllerTest {
  private SamsungRemote mSamsungRemoteMock;
  private SamsungTvController mController;

  @Before
  public void initialize() {
    mSamsungRemoteMock = mock(SamsungRemote.class);
    mController = new SamsungTvController(mSamsungRemoteMock, MoreExecutors.directExecutor());
  }

  @Test
  public void connectAndAuthenticateWhenNotConnected() {
    when(mSamsungRemoteMock.isConnected()).thenReturn(false);
    mController.switchOff();
    verify(mSamsungRemoteMock).connectAndAuthenticate();
  }

  @Test
  public void noReconnectWhenAlreadyConnected() {
    when(mSamsungRemoteMock.isConnected()).thenReturn(true);
    mController.switchOff();
    verify(mSamsungRemoteMock, never()).connectAndAuthenticate();
  }

  @Test
  public void ensureCorrectKeycodeIsSent() throws IOException {
    when(mSamsungRemoteMock.isConnected()).thenReturn(true);
    when(mSamsungRemoteMock.connectAndAuthenticate()).thenReturn(true);
    mController.switchOff();
    verify(mSamsungRemoteMock).sendKeycode(KeyCode.KEY_POWEROFF);
  }

  @Test
  public void noKeyCodeSentWhenConnectionFails() throws IOException {
    when(mSamsungRemoteMock.isConnected()).thenReturn(false);
    when(mSamsungRemoteMock.connectAndAuthenticate()).thenReturn(false);
    mController.switchOff();
    verify(mSamsungRemoteMock, never()).sendKeycode(KeyCode.KEY_POWEROFF);
  }
}
