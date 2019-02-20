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
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.s13g.winston.async.Executors;
import com.s13g.winston.async.Scope;
import com.s13g.winston.control.VoiceCommands;
import com.s13g.winston.controller.TileCreatorRegistry;
import com.s13g.winston.net.HttpRequester;
import com.s13g.winston.net.HttpRequesterImpl;
import com.s13g.winston.proto.nano.ForClients.ChannelData;
import com.s13g.winston.requests.ChannelDataRequester;
import com.s13g.winston.requests.ChannelValueRequester;
import com.s13g.winston.views.TiledViewCreator;
import com.s13g.winston.views.Toaster;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WinstonMainActivity extends Activity implements View.OnClickListener {
  private static final Logger LOG = Logger.getLogger("MainActivity");

  private Scope mActivityScope;
  private Toaster mToaster;
  private PreferenceManager mPreferenceManager;
  private VoiceCommands mVoiceCommands;
  private Executors mExecutors;
  private ChannelDataRequester mChannelDataRequest;
  private ChannelValueRequester mChannelValueRequester;
  private TileCreatorRegistry mTileCreatorRegistry;
  private TiledViewCreator mTiledViewCreator;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mActivityScope = new Scope();
    mExecutors = new Executors();

    mToaster = new Toaster(this);
    mPreferenceManager = new PreferenceManager(getApplicationContext());
    Optional<HttpRequester> httpRequester = HttpRequesterImpl.create(mPreferenceManager, mToaster);

    mVoiceCommands = VoiceCommands.create(getApplicationContext());

    // Don't continue, if we cannot initialize an http requester.
    if (!httpRequester.isPresent()) {
      return;
    }

    mChannelDataRequest = mActivityScope.add(
        new ChannelDataRequester(httpRequester.get(), mExecutors.getNetworkExecutor()));
    mChannelValueRequester = new ChannelValueRequester(httpRequester.get(), mExecutors
        .getNetworkExecutor());

    mTileCreatorRegistry = new TileCreatorRegistry(getApplicationContext(), mChannelValueRequester);
    mTiledViewCreator = new TiledViewCreator((ViewGroup) findViewById(R.id.tile_container),
        mTileCreatorRegistry.getCreators());
  }

  @Override
  protected void onStart() {
    super.onStart();

    if (mChannelDataRequest == null) {
      return;
    }

    ListenableFuture<ChannelData> channelData = mChannelDataRequest.execute();
    Futures.addCallback(channelData, new FutureCallback<ChannelData>() {
      @Override
      public void onSuccess(ChannelData data) {
        onSystemDataLoaded(data);
      }

      @Override
      public void onFailure(Throwable t) {
        LOG.log(Level.SEVERE, "Request failed", t);
        mToaster.showToast(t.getMessage(), Toaster.Duration.LONG);
      }
    }, mExecutors.getMainThreadExecutor());
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
      Intent intent = new Intent(this, SettingsActivity.class);
      startActivity(intent);
      return true;
    } else if (id == R.id.action_voice_command) {
      onStartVoiceControl();
      return true;
    } else if (id == R.id.action_refresh) {
      mTiledViewCreator.refreshAll();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onClick(final View view) {
    switch (view.getId()) {
    }
  }

  private void onSystemDataLoaded(final ChannelData channelData) {
    mExecutors.getMainThreadExecutor().execute(new Runnable() {
      @Override
      public void run() {
        mTiledViewCreator.addTiles(channelData);
      }
    });
  }

  private void onStartVoiceControl() {
    mToaster.showToast(getString(R.string.speak_now), Toaster.Duration.LONG);
    mVoiceCommands.onStart();
  }
}
