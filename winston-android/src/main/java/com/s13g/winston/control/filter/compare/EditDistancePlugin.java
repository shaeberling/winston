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

package com.s13g.winston.control.filter.compare;

import static com.s13g.winston.control.filter.StringComparisonFilter.Cell;

public class EditDistancePlugin implements IComparePlugin {

    /**
     * Initializes the row for the comparison
     *
     * @param cellCache the cache to initiate the comparison
     * @param pos       the position to initialize
     */
    @Override
    public void initRow(final Cell[][] cellCache, final int pos) {
        cellCache[0][pos].setCost(pos);
        if (pos > 0) {
            cellCache[0][pos].setParent(POSITION_INSERT);
        }
    }

    /**
     * Initializes the column for the comparison
     *
     * @param cellCache the cache to initiate the comparison
     * @param pos       the position to initialize
     */
    @Override
    public void columnInit(final Cell[][] cellCache, final int pos) {
        cellCache[pos][0].setCost(pos);
        if (pos > 0) {
            cellCache[pos][0].setParent(POSITION_DELETE);
        }
    }

    /**
     * Compares two given characters. If the characters are equal <code>0</code>
     * will be returned because no changes are necessary. If the characters are
     * not equal a weight (in our case <code>1</code>) will be returned showing
     * that a change is necessary
     *
     * @param character1 the first char for the comparison
     * @param character2 the second char for the comparison
     * @return equals = 0<br>
     * not equals = 1
     */
    @Override
    public int validateMatch(final char character1, final char character2) {
        return character1 == character2 ? 0 : 1;
    }

    /**
     * The weight that is necessary to insert or delete a given character, in
     * this case <code>1</code> equal to a non matching character
     *
     * @param character the character that has to be inserted / deleted
     * @return <code>1</code>
     */
    @Override
    public int alterationDistance(char character) {
        return 1;
    }

    /**
     * Determines the target cell to get the required edit distance between <code>string1</code>
     * and <code>string2</code>
     *
     * @param cellCache the cache containing the calculated values of the comparison
     * @param string1   string 1 for the comparison
     * @param string2   string 2 for the comparison
     * @param posI      posI
     * @param posJ      posJ
     * @return the edit distance of the comparison between <code>string1</code>
     * and <code>string2</code>
     */
    @Override
    public int determineDistanceResult(
            final Cell[][] cellCache,
            final String string1,
            final String string2,
            final int posI,
            final int posJ) {
        return (cellCache[string1.length() - 1][string2.length() - 1].getCost());
    }
}
