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
package com.s13g.winston.control.type;

import android.content.Context;

import com.s13g.winston.control.operation.IOperationProcess;

/**
 * The factory to create different possibilities to control winston (e.g. via voice or gestures)
 */
public class OperationFactoryImpl implements IOperationFactory {

    /**
     * Creates a operation to the given type of operation type. A operation represents the way how to interact
     * with winston (e.g. speech operation)
     *
     * @param context            the context to global information about the application environment
     * @param typeOfOperation    the type determines the operation to use
     * @param operationProcessor the operation processor processes / forwards the recognized input operations
     * @return the created operation to the given operation type
     */
    public IOperationType createOperation(
            final Context context,
            final OperationType typeOfOperation,
            final IOperationProcess operationProcessor) {

        IOperationType operation = null;
        if (typeOfOperation == OperationType.SPEECH_AUTOMATIC || typeOfOperation == OperationType.SPEECH_MANUAL) {
            operation = new SpeechOperation(context, operationProcessor, typeOfOperation == OperationType.SPEECH_AUTOMATIC);
        }
        return operation;
    }
}
