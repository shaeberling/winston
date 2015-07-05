/*
 * Copyright 2015 Michael Heymel
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
package com.s13g.winston.control.operation;

import android.content.Context;
import android.widget.Toast;

import com.s13g.winston.control.type.IOperationType;
import com.s13g.winston.control.type.IOperationFactory;

public class OperationBroker {
    private final Builder mBuilder;
    private IOperationType mOperation;

    /**
     * Constructor
     *
     * @param builder the builder to initialize the broker with the operation
     */
    private OperationBroker(final Builder builder) {
        mBuilder = builder;
        mOperation = mBuilder.mOperationBroker.createOperation(mBuilder.mContext,
                mBuilder.mOperationType,
                mBuilder.mOperationProcessor);
    }

    /**
     * Initiates the recognition of the operation class (e.g. speech or gestures) if the operation
     * is not active yet.
     */
    public synchronized void onStart() {
        if (mOperation.isActive() == false) {
            if (mOperation.startOperationRecognition() == false) {
                Toast.makeText(mBuilder.mContext, "Speech operation initialization error!", Toast.LENGTH_LONG).show(); // TODO string (or exception)
            }
        }
    }


    /**
     * Stops the recognition of the operation class.
     */
    public synchronized void onStop() {
        if (mOperation.stopOperationRecognition() == false) {
            Toast.makeText(mBuilder.mContext, "Speech operation stop error!", Toast.LENGTH_LONG).show(); // TODO string (or exception)
        }
    }


    public static class Builder {
        private Context mContext;
        private IOperationProcess mOperationProcessor;
        private IOperationFactory mOperationBroker;
        private IOperationFactory.OperationType mOperationType;

        /**
         * Constructor
         *
         * @param context the context to global information about the application environment
         */
        public Builder(final Context context) {
            mContext = context;
        }

        /**
         * Set the operation processor. The operation processor is used to process the determined operation types.
         *
         * @param operationProcessor the operation processor
         * @return this
         */
        public Builder setOperationProcessor(final IOperationProcess operationProcessor) {
            mOperationProcessor = operationProcessor;
            return this;
        }

        /**
         * Set the factory to create the operation
         *
         * @param operationFactory the factory to create the operation
         * @return this
         */
        public Builder setOperationFactory(final IOperationFactory operationFactory) {
            mOperationBroker = operationFactory;
            return this;
        }

        /**
         * Set the type of operation
         *
         * @param operationType the type of operation (e.g. Speech or Gesture)
         * @return this
         */
        public Builder setOperationType(final IOperationFactory.OperationType operationType) {
            mOperationType = operationType;
            return this;
        }

        /**
         * The builder method for this builder class. Creates the {@link OperationBroker} object
         *
         * @return the operation broker object
         */
        public OperationBroker build() {
            return new OperationBroker(this);
        }
    }
}
