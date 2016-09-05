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

package com.s13g.winston.lib.wemo;

import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Default implementation of a Wemo switch.
 */
class WemoSwitchImpl implements WemoSwitch {
  private static final Logger LOG = Logger.getLogger("WemoSwitch");
  private final String mFriendlyName;
  private final String mManufacturer;
  private final String mModelDescription;
  private final String mModelNumber;
  private final String mFirmwareVersion;
  private final String mSerialNumber;
  private final Function<Void, Optional<Boolean>> mQueryFunction;
  private final Function<Boolean, Boolean> mSwitchFunction;

  WemoSwitchImpl(String friendlyName, String manufacturer, String modelDescription,
                 String modelNumber, String firmwareVersion, String serialNumber,
                 Function<Void, Optional<Boolean>> queryFunction,
                 Function<Boolean, Boolean> switchFunction) {
    mFriendlyName = friendlyName;
    mManufacturer = manufacturer;
    mModelDescription = modelDescription;
    mModelNumber = modelNumber;
    mFirmwareVersion = firmwareVersion;
    mSerialNumber = serialNumber;
    mQueryFunction = queryFunction;
    mSwitchFunction = switchFunction;
  }

  @Override
  public Optional<Boolean> isOn() {
    return mQueryFunction.apply(null);
  }

  @Override
  public boolean setSwitch(boolean on) {
    return mSwitchFunction.apply(on);
  }

  @Override
  public String toString() {
    String toString = "=== Wemo Switch ===\n";
    toString += "Friendly name: " + mFriendlyName + "\n";
    toString += "Manufacturer : " + mManufacturer + "\n";
    toString += "Model        : " + mModelDescription + "\n";
    toString += "Model Number : " + mModelNumber + "\n";
    toString += "Firmware     : " + mFirmwareVersion + "\n";
    toString += "Serial number: " + mSerialNumber + "\n";
    return toString;
  }
}
