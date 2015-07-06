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
package com.s13g.winston.control.type;

import android.content.Context;

import com.s13g.winston.control.operation.IOperationProcess;

/**
 * The factory interface to create different possibilities to control winston
 * (e.g. via voice or gestures)
 */
public interface IOperationFactory {

    /**
     * Different types of commands
     */
    public enum OperationType {
        // speech has to be activated manually. No stop is required
        SPEECH_MANUAL,
        // speech has to be activated once. Also the command recognition must be stopped
        SPEECH_AUTOMATIC,
        GESTURE
    }

    /**
     * Creates an operation to control winston functions (e.g. access nodes)
     *
     * @param context          the context to global information about the application environment
     * @param typeOfOperation  the type determines the operation to use
     * @param commandProcessor the command processor processes / forwards the recognized commands
     * @return the command to control a winston function
     */
    public IOperationType createOperation(
            final Context context,
            final OperationType typeOfOperation,
            final IOperationProcess commandProcessor);
}
