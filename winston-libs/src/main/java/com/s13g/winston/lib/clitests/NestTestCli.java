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

package com.s13g.winston.lib.clitests;

import com.google.common.io.Files;
import com.s13g.winston.lib.nest.NestAuthenticator;
import com.s13g.winston.lib.nest.NestController;
import com.s13g.winston.lib.nest.NestControllerImpl;
import com.s13g.winston.lib.nest.data.NestResponseParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A CLI to test the Nest API.
 */
public class NestTestCli {
  public static void main(String[] args) throws IOException {
    // requestAccessToken();
    String accessToken = readFile("/Users/haeberling/nest-data/access-token");
    System.out.println("Access token: '" + accessToken + "'.");

    NestResponseParser parser = new NestResponseParser();
    NestController controller = new NestControllerImpl("Bearer " + accessToken, parser);
    controller.getAllThermostats();
  }

  /** Call this to get the access token that is then used in successive requests to the Nest API. */
  private static void requestAccessToken() throws IOException {
    String productId = readFile("/Users/haeberling/nest-data/product-id");
    String productSecret = readFile("/Users/haeberling/nest-data/product-secret");
    String userPinCode = readFile("/Users/haeberling/nest-data/user-pin-code");

    System.out.println("Product ID    : '" + productId + "'");
    System.out.println("Product Secret: '" + productSecret + "'");
    System.out.println("User PIN code : '" + userPinCode + "'");
    NestAuthenticator authenticator = new NestAuthenticator();
    authenticator.getAccessToken(productId, userPinCode, productSecret);
  }

  private static String readFile(String file) throws IOException {
    return Files.toString(new File(file), Charset.defaultCharset()).trim();
  }
}
