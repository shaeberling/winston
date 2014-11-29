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

package com.s13.winston;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.s13.winston.GarageStatusFuture.GarageStatus;

public class MainActivity extends Activity {
	private static final Logger LOG = Logger.getLogger("MainActivity");
	private static final String SERVER_URL = "http://192.168.1.120:1984/io/%s";
	private static final String RELAY_CLICK_PARAM = "relay/%d/2";
	private static final String REED_STATUS_PARAM = "reed/%d";
	private final ExecutorService mPool = Executors.newFixedThreadPool(15);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button actionGarage1 = (Button) findViewById(R.id.action_garage_1);
		actionGarage1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clickGarage(0);
			}
		});
		Button actionGarage2 = (Button) findViewById(R.id.action_garage_2);
		actionGarage2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clickGarage(1);
			}
		});

		final TextView statusGarage1 = (TextView) findViewById(R.id.status_garage_1);
		statusGarage1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Future<GarageStatus> result = getGarageStatus(0, 1);
				mPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							final GarageStatus status = result.get();
							statusGarage1.post(new Runnable() {
								@Override
								public void run() {
									statusGarage1.setText(GarageStatus
											.toStringId(status));
								}
							});
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
					}
				});
			}
		});

		TextView statusGarage2 = (TextView) findViewById(R.id.status_garage_2);
		statusGarage2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO...
			}
		});
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

	private void clickGarage(int num) {
		String params = String.format(RELAY_CLICK_PARAM, num);
		final String url = String.format(SERVER_URL, params);
		mPool.execute(new Runnable() {
			@Override
			public void run() {
				requestUrl(url);
			}
		});
	}

	private Future<GarageStatus> getGarageStatus(int openReed, int closedReed) {
		final String urlOpen = String.format(SERVER_URL,
				String.format(REED_STATUS_PARAM, openReed));
		final String urlClosed = String.format(SERVER_URL,
				String.format(REED_STATUS_PARAM, closedReed));

		Future<Boolean> openReedResult = getReedStatus(urlOpen);
		Future<Boolean> closedReedResult = getReedStatus(urlClosed);
		return new GarageStatusFuture(openReedResult, closedReedResult);
	}

	private Future<Boolean> getReedStatus(final String reedUrl) {
		return mPool.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				String result = requestUrl(reedUrl);
				LOG.info("status reed result: " + result);
				return !result.trim().equals("0");
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
