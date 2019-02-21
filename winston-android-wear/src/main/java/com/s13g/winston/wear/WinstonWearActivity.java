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

package com.s13g.winston.wear;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.common.flogger.FluentLogger;
import com.s13g.winston.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main activity for the Winston Android Wear app.
 */
public class WinstonWearActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private GoogleApiClient mGoogleApiClient;
    private ExecutorService mPool;
    private boolean mGmsConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wear_activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addApi(Wearable.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();

        setupListener(R.id.action_light, "/light/1");
        setupListener(R.id.action_garage_1, "/garage/0");
        setupListener(R.id.action_garage_2, "/garage/1");
    }

    private void setupListener(int buttonId, final String path) {
        Button actionLight = (Button) findViewById(buttonId);
        actionLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToAllNodes(path);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPool = Executors.newFixedThreadPool(2);
        mGmsConnected = false;
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGmsConnected = false;
        mGoogleApiClient.disconnect();
        mPool.shutdown();
        super.onStop();
    }

    @Override
    // Google Play Services
    public void onConnected(Bundle bundle) {
        log.atInfo().log("Google Play Services connected");
        mGmsConnected = true;
    }

    @Override
    // Google Play Services
    public void onConnectionSuspended(int i) {
        log.atInfo().log("Google Play Services connection suspended");
        mGmsConnected = false;
    }

    @Override
    // Google Play Services
    public void onConnectionFailed(ConnectionResult connectionResult) {
        log.atWarning().log("Google Play Services connectione failed");

    }

    private void sendMessageToAllNodes(final String path) {
        log.atInfo().log("About to send message to all nodes: " + path);
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                for (Node node : nodes.getNodes()) {
                    sendMessageToNode(path, node);
                }
            }
        });
    }

    private void sendMessageToNode(String path, Node node) {
        log.atInfo().log("Sending to node: " + node.getId() + " (" + node.getDisplayName() + ")");
        Wearable.MessageApi.sendMessage(
            mGoogleApiClient, node.getId(), path, null).setResultCallback(
            new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                    if (!sendMessageResult.getStatus().isSuccess()) {
                        log.atWarning().log("Failed to send message with status code: "
                            + sendMessageResult.getStatus().getStatusCode());
                    } else {
                        log.atInfo().log("Sending message: success");
                    }
                }
            }
        );
    }

}