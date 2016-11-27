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

package com.s13g.winston;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

/**
 * Preferences for the Winston app.
 */
public class PreferenceManager {

  private final SharedPreferences mSharedPreferences;

  public PreferenceManager(Context context) {
    mSharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
  }


  public Optional<String> getServer() {
    Optional<String> serverOpt = getString("pref_key_server");
    if (!serverOpt.isPresent() || Strings.isNullOrEmpty(serverOpt.get())) {
      return Optional.absent();
    }

    String server = serverOpt.get();
    if (server.contains("://")) {
      return Optional.of(server);
    }
    return Optional.of("https://" + server);
  }

  public Optional<String> getAuthtoken() {
    return getString("pref_key_auth_token");
  }

  private Optional<String> getString(String key) {
    return Optional.fromNullable(mSharedPreferences.getString(key, null));
  }
}
