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

import com.s13g.winston.control.filter.compare.IComparePlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.s13g.winston.control.filter.compare.IComparePlugin.POSITION_DELETE;
import static com.s13g.winston.control.filter.compare.IComparePlugin.POSITION_INSERT;
import static com.s13g.winston.control.filter.compare.IComparePlugin.POSITION_MATCH;

/**
 * Defines a string comparison filter with the possibility for multiple filters (e.g. EditDistance,
 * SubstringMatching, LCS).
 */
public class StringComparisonFilter implements IOperationFilter {

  private final int[] mComparisonOption = new int[3];
  private final static int ILLEGAL_MATCH = -1;
  private final IComparePlugin mFilter;
  private final String[] mKnownCommandOperations;
  private final int mTolerance;

  /**
   * Constructor
   *
   * @param filter
   *     the filter to use for the string comparison.
   * @param knownOperations
   *     an array of known words. The filter method will use this word
   * @param tolerance
   *     this value represents the allowed variance (equal or less) of the string comparison with
   *     known strings. For simplicity, currently only one tolerance value to all known data is
   *     supported. A better way would be to create a tolerance for each data string
   */
  public StringComparisonFilter(final IComparePlugin filter, final String[] knownOperations, final int tolerance) {
    mFilter = filter;
    mKnownCommandOperations = knownOperations;
    mTolerance = tolerance;
  }


  /**
   * Filters the list of all input value within all known command operations using the specified
   * {@IComparePlugin} and the <code>tolerance</code>. In case the perfect matching command
   * operation (=0 difference) was found , this method stops the lookup
   *
   * @param input
   *     the list with all input values to filter
   * @return a list of filtered input command operations
   */
  @Override
  public List<String> filter(List<String> input) {

    if (input != null && mKnownCommandOperations != null && mKnownCommandOperations.length > 0) {
      boolean finished = false;
      final Iterator<String> inputIterator = input.iterator();
      while (inputIterator.hasNext() && finished == false) {

        String operation = inputIterator.next();
        int compareResult = 0;
        boolean valid = false;
        for (int i = 0; i < mKnownCommandOperations.length && valid == false; ++i) {

          compareResult = compare(operation, mKnownCommandOperations[i]);
          valid = compareResult != ILLEGAL_MATCH && compareResult <= mTolerance;
        }

        // the variation of the command operation is too high, so it is an "unknown" command and will
        // be removed
        if (valid == false) {
          inputIterator.remove();
        }
        // found the perfect matching operation
        else if (compareResult == 0) {
          input = new ArrayList<>(1);
          input.add(operation);
          finished = true;
        }
      }
    }
    return input;
  }

  /**
   * This method compares the two given strings <code>string1</code>, <code>string2</code> and
   * returns the number of required changes that have to be  done to match both given strings. To
   * match both given strings three actions may be possible:<br> - REPLACE (replace a character with
   * an other)<br> - INSERT (insert a new character)<br> - DELETE (delete an existing character)<br>
   * <br> The string comparison function returns the cost of the optimal alignment, but not the
   * alignment itself. <br> An empty string counts as <code>null</code> string. <br> If for example
   * "you should not" and "thou shalt not" have to be compared the result would be 5.
   *
   * @param string1
   *     string 1 for the comparison
   * @param string2
   *     string 2 for the comparison
   * @return number of changes
   */
  private int compare(final String string1, final String string2) {
    int distance = ILLEGAL_MATCH;
    if (string1 == null && string2 == null) {
      distance = ILLEGAL_MATCH;
    } else if (string1 != null && string2 == null) {
      distance = string1.length() == 0 ? ILLEGAL_MATCH : string1.length();
    } else if (string1 == null) {
      distance = string2.length() == 0 ? ILLEGAL_MATCH : string2.length();
    } else {
      distance = determineDistance(string1, string2);
    }
    return distance;
  }


  /**
   * This method compares two given strings <code>string1</code> and  <code>string2</code> and
   * returns the number of changes that have to be  done to match both given strings. To match both
   * given strings three actions may be possible:<br> - REPLACE (replace a character with an
   * other)<br> - INSERT (insert a new character)<br> - DELETE (delete an existing character)<br>
   * <br> The string comparison function returns the cost of the optimal alignment, but not the
   * alignment itself. <br> <br> If for example "you should not" and "thou shalt not" have to be
   * compared the result would be 5.
   *
   * @param string1
   *     string 1 for the comparison
   * @param string2
   *     string 2 for the comparison
   * @return number of changes
   */
  private int determineDistance(String string1, String string2) {
    // expect the string with a blank beginning to keep the matrix m indices
    // in sync with those of the strings for clarity.
    string1 = " " + string1;
    string2 = " " + string2;
    // determine the max string length. Based on this the cache will be
    // sized
    final int maxLen = string1.length() > string2.length() ?
        string1.length() : string2.length();

    // initializes the cell matrix that contains the cells to remember the
    // comparison results
    final Cell[][] cellCache = initCellMatrix(maxLen);

    int i = 0;
    int j = 0;
    // start the calculation at the second character due to the insertion of
    // the blank before the actual string
    for (i = 1; i < string1.length(); ++i) {
      for (j = 1; j < string2.length(); ++j) {
        // try to match the characters of the last current string position. If they
        // don't match, a character have to be possibly replaced. Also the cost of the last
        // string position have to be added
        mComparisonOption[POSITION_MATCH] = cellCache[i - 1][j - 1].getCost() +
            mFilter.validateMatch(string1.charAt(i), string2.charAt(j));
        // calculate the change if a character has to be inserted based on the last
        // calculation result.
        mComparisonOption[POSITION_INSERT] = cellCache[i][j - 1].getCost() +
            mFilter.alterationDistance(string2.charAt(j));
        // calculate the change if a character has to be deleted based on the last
        // calculation result.
        mComparisonOption[POSITION_DELETE] = cellCache[i - 1][j].getCost() +
            mFilter.alterationDistance(string1.charAt(i));

        // identify the lowest cost of MATCH, INSERT and DELETE. Also set as parent the
        // operation that was done
        cellCache[i][j].setParent(POSITION_MATCH);
        cellCache[i][j].setCost(mComparisonOption[POSITION_MATCH]);
        for (int k = POSITION_INSERT; k <= POSITION_DELETE; ++k) {
          if (mComparisonOption[k] < cellCache[i][j].getCost()) {
            cellCache[i][j].setCost(mComparisonOption[k]);
            cellCache[i][j].setParent(k);
          }
        }
      }
    }
    return mFilter.determineDistanceResult(cellCache, string1, string2, i, j);
  }


  /**
   * Initializes the two dimensional cache array for the dynamic programming based on the given
   * <code>maxLen</code> value
   *
   * @param maxLen
   *     the maximum length of the cache
   * @return the DP cache to store the the comparison results.
   */
  private Cell[][] initCellMatrix(final int maxLen) {
    final Cell[][] cellCache = new Cell[maxLen][maxLen];
    for (int i = 0; i < maxLen; ++i) {
      for (int j = 0; j < maxLen; ++j) {
        cellCache[i][j] = new Cell();
      }
    }

    // initializes the first row and the first column
    for (int i = 0; i < maxLen; ++i) {
      mFilter.initRow(cellCache, i);
      mFilter.columnInit(cellCache, i);
    }
    return cellCache;
  }


  /**
   * A cell will be used to store all values of the comparison to enable DP
   */
  public static final class Cell {
    private int mCost = -1;
    private int mParent = -1;

    /**
     * Set the distance cost
     *
     * @param cost
     *     the distance cost
     */
    public void setCost(final int cost) {
      mCost = cost;
    }

    /**
     * @return the distance cost
     */
    public int getCost() {
      return mCost;
    }

    /**
     * Set the parent position
     *
     * @param parent
     *     the parent position
     */
    public void setParent(final int parent) {
      mParent = parent;
    }

    /**
     * @return the parent position
     */
    public int getParent() {
      return mParent;
    }
  }
}
