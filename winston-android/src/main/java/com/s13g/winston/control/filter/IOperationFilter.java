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
package com.s13g.winston.control.filter;

import java.util.List;

/**
 * Represents the filter to match operation inaccuracies (e.g. words that have a small
 * inaccuracy to a specified operation).
 * <p/>
 * Note: Currently the system is only designed to set no filter or one filter. If required,
 * a more sophisticated filter layer could be designed via a composite pattern.
 */
public interface IOperationFilter {

    /**
     * Depending on the implemeting comparison strategy, the input values in the list will be filtered.
     * Due to this, more accurate operation can be processed.
     *
     * @param input the list with all input values to filter
     * @return a list of filtered input values
     */
    public List<String> filter(final List<String> input);
}