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

package com.s13g.winston.wear;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.s13g.winston.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Main activity for the Winston Android Wear app.
 */
public class WinstonWearActivity extends Activity {
    private static final Logger LOG = Logger.getLogger("WinstonWearActivity");
    // TODO: This needs to be configured dynamically.
    private static final String LIGHT_NODE_URL = "http://192.168.1.201:1984/io/%s";
    private static final String RELAY_CLICK_PARAM = "relay/%d/2";
    private static final String REED_STATUS_PARAM = "reed/%d";

    private ExecutorService mPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wear_activity_main);

        Button actionLight = (Button) findViewById(R.id.action_light);
        actionLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRelay(LIGHT_NODE_URL, 0);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPool = Executors.newFixedThreadPool(2);
    }

    @Override
    protected void onPause() {
        mPool.shutdown();
        super.onPause();
    }

    private void clickRelay(String nodeUrl, int num) {
        String params = String.format(RELAY_CLICK_PARAM, num);
        final String url = String.format(nodeUrl, params);
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                String result = requestUrl(url);
                LOG.info("Request Result: result");
            }
        });
    }

    // TODO: All of this needs to be put in common classes since it's shared with the main app.
    // Maybe think about relaying this through the app to have the code in one place only.
    private static String requestUrl(String rpcUrl) {
        LOG.info("rpcUrl: " + rpcUrl);
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