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

public interface IOperationType {

    /**
     * Starts the recognition for this type of operation
     *
     * @return <code>true</code>the recognition was started successfully<br>
     *      <code>false</code> the recognition wasn't started
     */
    public boolean startOperationRecognition();

    /**
     * Stops the recognition for this type of operation
     *
     * @return <code>true</code>the recognition was stopped successfully<br>
     *      <code>false</code> the recognition wasn't stopped
     */
    public boolean stopOperationRecognition();

    /**
     * @return <code>true</code> the command is currently active and awaits input<br>
     *     <code>false</code> the command is not active
     */
    public boolean isActive();
}
