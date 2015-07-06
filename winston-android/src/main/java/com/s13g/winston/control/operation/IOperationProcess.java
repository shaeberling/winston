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
package com.s13g.winston.control.operation;

import java.util.List;

/**
 * Represents the interface to process the recognized (and eventually filtered) commands, e.g.
 * forward send them to the server side
 */
public interface IOperationProcess {

    /**
     * This method processes recognized operation results. In case an optional filter is defined,
     * the recognized words operations will be filtered and then processed
     *
     * @param recognizedOperations the results with all recognized words
     * @param confidenceScore      the confidence score of the recognized operations / words (currently unused)
     */
    public void processRecognizedOperationResults(List<String> recognizedOperations, final float[] confidenceScore);
}
