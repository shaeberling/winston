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

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.s13g.winston.requests.NodeRequests;

import java.util.logging.Logger;

public class WinstonMainActivity extends Activity {
    private static final Logger LOG = Logger.getLogger("PowerStripAct");
    private NodeRequests mNodeRequests;

    // TODO: Read switch state!
    private final boolean[] currentSwitchState = new boolean[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button actionGarage1 = (Button) findViewById(R.id.action_garage_0);
        actionGarage1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mNodeRequests.execute("/garage/0");
            }
        });
        Button actionGarage2 = (Button) findViewById(R.id.action_light);
        actionGarage2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mNodeRequests.execute("/light/1");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mNodeRequests = new NodeRequests();
    }

    @Override
    protected void onStop() {
        mNodeRequests.close();
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
        }
        return super.onOptionsItemSelected(item);
    }
}
