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

package com.s13g.winston.lib.core.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * HTTP request utility methods.
 */
public class HttpUtil {
  /**
   * Makes a GET request to the given URL.
   *
   * @param rpcUrl the give HTTP URL
   * @return Response received from the request.
   */
  public static String requestUrl(String rpcUrl) throws IOException {
    return requestUrl(rpcUrl, Method.GET);
  }

  /**
   * Makes a request to the given URL.
   *
   * @param rpcUrl the give HTTP URL
   * @param method which method to use for the request.
   * @return Response received from the request.
   */
  public static String requestUrl(String rpcUrl,
                                  HttpUtil.Method method) throws IOException {
    return requestUrl(rpcUrl, method, ContentType.NONE, "");
  }

  /**
   * Makes a request to the given URL.
   *
   * @param rpcUrl the give HTTP URL
   * @param method which method to use for the request.
   * @param contentType sets the content type header.
   * @return Response received from the request.
   */
  public static String requestUrl(String rpcUrl,
                                  HttpUtil.Method method,
                                  HttpUtil.ContentType contentType,
                                  String authorization) throws IOException {
    return requestUrl(rpcUrl, method, contentType, authorization, Optional.empty());
  }

  /**
   * Makes a request to the given URL.
   *
   * @param rpcUrl the give HTTP URL
   * @param method which method to use for the request.
   * @param contentType sets the content type header.
   * @return Response received from the request.
   */
  public static String requestUrl(String rpcUrl,
                                  HttpUtil.Method method,
                                  HttpUtil.ContentType contentType,
                                  String authorization,
                                  Optional<String> data) throws IOException {
    Map<String, String> headers = new HashMap<>();

    if (contentType != ContentType.NONE) {
      headers.put("Content-Type", contentType.str);
    }
    if (authorization != null && !authorization.isEmpty()) {
      headers.put("Authorization", authorization);
    }
    return requestUrl(rpcUrl, method, headers, data);
  }

  /**
   * Makes a request to the given URL.
   *
   * @param rpcUrl the give HTTP URL
   * @param method which method to use for the request.
   * @param header sets the headers for this request.
   * @return Response received from the request.
   */
  public static String requestUrl(String rpcUrl,
                                  Method method,
                                  Map<String, String> header,
                                  Optional<String> data) throws IOException {
    StringBuilder resultStr = new StringBuilder();
    try {
      final HttpURLConnection connection = (HttpURLConnection) (new URL(
          rpcUrl)).openConnection();
      connection.setRequestMethod(method.methodStr);
      for (String key : header.keySet()) {
        String value = header.get(key);
        connection.setRequestProperty(key, value);
      }

      connection.setUseCaches(false);
      connection.setDoOutput(true);
      if (data.isPresent()) {
        connection.setDoInput(true);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection
            .getOutputStream()));
        writer.append(data.get());
        writer.flush();
        writer.close();
      }

      BufferedReader reader = new BufferedReader(new InputStreamReader(
          connection.getInputStream()));
      String line;
      boolean first = true;
      while ((line = reader.readLine()) != null) {
        if (first) {
          first = false;
        } else {
          resultStr.append('\n');
        }
        resultStr.append(line);
      }
      reader.close();
    } catch (final MalformedURLException e) {
      throw new IOException("HTTP request failed. Malformed URL.");
    } catch (final IOException e) {
      throw new IOException("HTTP request failed.", e);
    }
    return resultStr.toString();
  }

  public enum Method {
    POST("POST"), GET("GET"), PUT("PUT");

    final String methodStr;

    Method(String methodStr) {
      this.methodStr = methodStr;
    }
  }

  public enum ContentType {
    JSON("application/json"), NONE("");

    final String str;

    ContentType(String contentTypeStr) {
      this.str = contentTypeStr;
    }
  }
}
