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
package com.s13g.winston.control.filter.compare;

import static com.s13g.winston.control.filter.StringComparisonFilter.Cell;

/**
 * Represents a comparison plugin for algorithms like EditDistance, LCS and Substring Matching
 */
public interface IComparePlugin {
    public final static int POSITION_MATCH = 0;
    public final static int POSITION_INSERT = 1;
    public final static int POSITION_DELETE = 2;

    /**
     * Initializes the row for the comparison
     *
     * @param cellCache the cache to initiate the comparison
     * @param pos       the position to initialize
     */
    public void initRow(final Cell[][] cellCache, final int pos);

    /**
     * Initializes the column for the comparison
     *
     * @param cellCache the cache to initiate the comparison
     * @param pos       the position to initialize
     */
    public void columnInit(final Cell[][] cellCache, final int pos);

    /**
     * Validates the match of <code>character1</code> and <code>character2</code>.
     *
     * @param character1 the first char for the comparison
     * @param character2 the second char for the comparison
     * @return equals = 0<br>
     * not equals = 1
     */
    public int validateMatch(final char character1, final char character2);

    /**
     * The weight that is necessary to insert or delete a given character
     *
     * @param character the character that has to be inserted / deleted
     * @return the distance
     */
    public int alterationDistance(final char character);

    /**
     * Determines the target cell to get the comparison result of <code>string1</code>
     * and <code>string2</code>.
     *
     * @param cellCache the cache containing the calculated values of the comparison
     * @param string1   string 1 for the comparison
     * @param string2   string 2 for the comparison
     * @param posI      posI
     * @param posJ      posJ
     * @return the result of the comparison between <code>string1</code>
     * and <code>string2</code>
     */
    public int determineDistanceResult(
            final Cell[][] cellCache,
            final String string1,
            final String string2,
            final int posI,
            final int posJ);
}
