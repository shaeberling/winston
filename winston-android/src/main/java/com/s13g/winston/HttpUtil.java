/*
 * Copyright 2015 Sascha Haeberling
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

package com.s13g.winston;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * HTTP request utility methods.
 */
public class HttpUtil {
    private static final Logger LOG = Logger.getLogger("HttpUtil");

    /**
     * Makes a request to the given URL.
     *
     * @param rpcUrl the give HTTP URL
     * @return Response received from the request.
     */
    public static String requestUrl(String rpcUrl) {
        LOG.info("requestUrl: " + rpcUrl);
        StringBuffer resultStr = new StringBuffer();
        try {
            final HttpURLConnection connection = (HttpURLConnection) (new URL(
                rpcUrl)).openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first == true) {
                    first = false;
                } else {
                    resultStr.append('\n');
                }
                resultStr.append(line);
            }
            reader.close();
        } catch (final MalformedURLException e1) {
            e1.printStackTrace();
        } catch (final IOException e2) {
            e2.printStackTrace();
        }
        return resultStr.toString();
    }

}
