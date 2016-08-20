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

package com.s13g.winston.lib.nest;

import com.s13g.winston.lib.core.net.HttpUtil;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Deals with authenticating with Nest to get the credentials needed to interact with the Nest API.
 */
public class NestAuthenticator {
  private static final Logger LOG = Logger.getLogger("NestAuthenticator");

  /**
   * User browses to this URL to get a PIN code.
   * <p>
   * Parameter:product-id
   */
  private static final String AUTH_URL = "https://home.nest" +
      ".com/login/oauth2?client_id=%s&state=STATE";

  /**
   * Request to get the access token.
   * <p>
   * Parameters: product-id, PIN code, product-secret.
   */
  private static final String ACCESS_TOKEN_URL = "https://api.home.nest.com/oauth2/access_token" +
      "?client_id=%s&code=%s&client_secret=%s&grant_type=authorization_code";

  /**
   * Requests the access token.
   *
   * @param clientId the client/product ID.
   * @param pinCode the user's PIN code, obtaine through the AUTH_URL above.
   * @param clientSecret the client/product secret.
   * @return Response in the form of: "'{"access_token":"...","expires_in":315360000}'"
   * @throws IOException Thrown if the request failed for some reason.
   */
  public String getAccessToken(String clientId,
                               String pinCode,
                               String clientSecret) throws IOException {
    String url = String.format(
        Locale.getDefault(), ACCESS_TOKEN_URL, clientId, pinCode, clientSecret);
    String response = HttpUtil.requestUrl(url, HttpUtil.Method.POST);
    LOG.info("Access token response: '" + response + "'");
    return response;
  }
}
