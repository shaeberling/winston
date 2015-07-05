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
import android.widget.Button;
import android.widget.Toast;

import com.s13g.winston.control.filter.IOperationFilter;
import com.s13g.winston.control.filter.StringComparisonFilter;
import com.s13g.winston.control.filter.compare.EditDistancePlugin;
import com.s13g.winston.control.operation.IOperationProcess;
import com.s13g.winston.control.operation.OperationBroker;
import com.s13g.winston.control.type.IOperationFactory;
import com.s13g.winston.control.type.OperationFactoryImpl;
import com.s13g.winston.requests.NodeRequests;

import java.util.List;
import java.util.logging.Logger;

public class WinstonMainActivity extends Activity implements View.OnClickListener, IOperationProcess {
    private static final Logger LOG = Logger.getLogger("WinstonMainActivity");
    private NodeRequests mNodeRequests;
    // control
    private static final int TOLERANCE = 1;
    private OperationBroker mCommandBroker;
    // TODO: currently, commands are only for test purposes
    private static final String[] KNOWN_WORDS = {"light on", "light off", "open garage", "close garage"};

    private IOperationFilter mOperationFilter;

    // TODO: Read switch state!
    private final boolean[] currentSwitchState = new boolean[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button voiceControl = (Button) findViewById(R.id.voice_control);
        voiceControl.setOnClickListener(this);
        Button actionGarage1 = (Button) findViewById(R.id.action_garage_0);
        actionGarage1.setOnClickListener(this);
        Button actionGarage2 = (Button) findViewById(R.id.action_light);
        actionGarage2.setOnClickListener(this);

        mOperationFilter = new StringComparisonFilter(new EditDistancePlugin(), KNOWN_WORDS, TOLERANCE);

        mCommandBroker = new OperationBroker.Builder(getApplicationContext())
                .setOperationFactory(new OperationFactoryImpl())
                .setOperationProcessor(this)
                .setOperationType(IOperationFactory.OperationType.SPEECH_MANUAL)
                .build();
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

    @Override
    public void onClick(final View view) {

        switch (view.getId()) {
            case R.id.voice_control:
                onStartVoiceControl();
                break;
            case R.id.action_garage_0:
                onActionCloseGarage();
                break;
            case R.id.action_light:
                onActionLightOn();
                break;
        }
    }

    private void onStartVoiceControl() {
        Toast.makeText(getApplicationContext(), "Speek now!", Toast.LENGTH_LONG).show();
        mCommandBroker.onStart();
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


    @Override
    public void processRecognizedOperationResults(List<String> recognizedOperations, float[] confidenceScore) {

        recognizedOperations = mOperationFilter.filter(recognizedOperations);

        if (recognizedOperations.size() == 1) {
            final String operation = recognizedOperations.get(0);
            // TODO: operation only for test purposes and also doubled (see above)
            if (operation.equals("light on")) {
                onActionLightOn();
            } else if (operation.equals("light off")) {
                onActionLightOff();
            } else if (operation.equals("open garage")) {
                onActionOpenGarage();
            } else if (operation.equals("close garage")) {
                onActionCloseGarage();
            } else {
                LOG.warning("unkown operation");
            }
        } else {
            LOG.info("no command processing");
        }
    }
}
