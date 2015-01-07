/*
 * Copyright 2014 Sascha Haeberling
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PowerStripActivity extends Activity {
	private static final Logger LOG = Logger.getLogger("PowerStripAct");
  // private static final String SERVER_URL = "http://192.168.1.201:1984/io/%s";
  private static final String SERVER_URL = "http://192.168.1.202:1984/io/%s";
	private static final String RELAY_SWITCH_PARAM = "relay/%d/%d";
	private ExecutorService mPool;

	// TODO: Read switch state!
	private final boolean[] currentSwitchState = new boolean[4];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button actionGarage1 = (Button) findViewById(R.id.action_garage_1);
		actionGarage1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleSwitch(0);
			}
		});
		Button actionGarage2 = (Button) findViewById(R.id.action_garage_2);
		actionGarage2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleSwitch(1);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPool = Executors.newFixedThreadPool(15);
	}

	@Override
	protected void onPause() {
		mPool.shutdown();
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void toggleSwitch(int num) {
	  // Toggle.
	  currentSwitchState[num] = !currentSwitchState[num];
	  int newState = currentSwitchState[num] ? 1 : 0;

		String params = String.format(RELAY_SWITCH_PARAM, num, newState);
		final String url = String.format(SERVER_URL, params);
		mPool.execute(new Runnable() {
			@Override
			public void run() {
				requestUrl(url);
			}
		});
	}


	private String requestUrl(String rpcUrl) {
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
