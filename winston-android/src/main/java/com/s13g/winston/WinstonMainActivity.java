/*
 * Copyright 2015 The Winston Authors
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

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.s13g.winston.async.Executors;
import com.s13g.winston.async.Scope;
import com.s13g.winston.control.VoiceCommands;
import com.s13g.winston.net.SystemDataLoader;
import com.s13g.winston.net.SystemDataLoaderForTesting;
import com.s13g.winston.net.SystemDataLoaderImpl;
import com.s13g.winston.proto.nano.ForClients;
import com.s13g.winston.requests.NodeRequests;
import com.s13g.winston.views.TiledViewCreator;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WinstonMainActivity extends Activity implements View.OnClickListener {
  private static final Logger LOG = Logger.getLogger("WinstonMainActivity");
  private static final boolean TEST_MODE_ENABLED = true;

  private Scope mActivityScope;
  private NodeRequests mNodeRequests;
  private VoiceCommands mVoiceCommands;
  private Executors mExecutors;
  private SystemDataLoader mSystemDataLoader;
  private TiledViewCreator mTiledViewCreator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mActivityScope = new Scope();
    mExecutors = new Executors();
    mVoiceCommands = VoiceCommands.create(getApplicationContext());
    if (TEST_MODE_ENABLED) {
      mSystemDataLoader = new SystemDataLoaderForTesting(mExecutors.getNetworkExecutor());
    } else {
      mSystemDataLoader = new SystemDataLoaderImpl(mExecutors.getNetworkExecutor());
    }
    mActivityScope.add(mSystemDataLoader);
    mTiledViewCreator = new TiledViewCreator((ViewGroup) findViewById(R.id.tile_container));
  }

  @Override
  protected void onStart() {
    super.onStart();
    mNodeRequests = new NodeRequests();
    mActivityScope.add(mNodeRequests);

    ListenableFuture<ForClients.SystemData> systemData = mSystemDataLoader.loadSystemData();
    Futures.addCallback(systemData, new FutureCallback<ForClients.SystemData>() {
      @Override
      public void onSuccess(ForClients.SystemData result) {
        onSystemDataLoaded(result);
      }

      @Override
      public void onFailure(Throwable t) {
        // TODO: Show an error message.
      }
    });
  }

  @Override
  protected void onStop() {
    try {
      mActivityScope.close();
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Error while closing scope.", e);
    }
    super.onStop();
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
    } else if (id == R.id.action_voice_command) {
      onStartVoiceControl();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onClick(final View view) {

    switch (view.getId()) {
      case R.id.action_garage_0:
        onActionCloseGarage();
        break;
      case R.id.action_light:
        onActionLightOn();
        break;
    }
  }

  private void onSystemDataLoaded(ForClients.SystemData systemData) {
    mTiledViewCreator.addTiles(systemData.ioChannel);
  }

  private void onStartVoiceControl() {
    Toast.makeText(
        getApplicationContext(), getString(R.string.speak_now), Toast.LENGTH_LONG).show();
    mVoiceCommands.onStart();
  }

  private void onActionCloseGarage() {
    mNodeRequests.execute("/garage/1");
  }

  private void onActionOpenGarage() {
    mNodeRequests.execute("/garage/1");
  }

  private void onActionLightOff() {
    mNodeRequests.execute("/light/1");
  }

  private void onActionLightOn() {
    mNodeRequests.execute("/light/1");
  }
}
