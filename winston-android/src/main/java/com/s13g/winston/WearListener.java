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

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.s13g.winston.requests.NodeRequests;

import java.util.logging.Logger;

/**
 * Listens to messages from the Winston wear app.
 */
public class WearListener extends WearableListenerService {
    private static final Logger LOG = Logger.getLogger("MainActivity");
    private NodeRequests mNodeRequests;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        LOG.info("Wear message received: " + messageEvent.getPath());
        if (mNodeRequests == null) {
            mNodeRequests = new NodeRequests();
        }
        mNodeRequests.execute(messageEvent.getPath());
    }

    @Override
    public void onDestroy() {
        if (mNodeRequests != null) {
            mNodeRequests.close();
            mNodeRequests = null;
        }
        super.onDestroy();
    }
}
