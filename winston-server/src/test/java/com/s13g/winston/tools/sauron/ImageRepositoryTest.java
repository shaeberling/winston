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

package com.s13g.winston.tools.sauron;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for ImageRepository
 */
public class ImageRepositoryTest {

  @Test
  public void testFileName() {
    // Time this test was written. Cheers!
    assertFileForDate(
        absFile("dev", "null", "foo", "2015", "06", "27", "23_54_42__000000000.jpg"),
        absFile("dev", "null", "foo"),
        2015, 6, 27, 23, 54, 42, 0);

    // Make sure padding is right everywhere.
    assertFileForDate(
        absFile("dev", "null", "foo", "0001", "02", "03", "04_05_06__000000007.jpg"),
        absFile("dev", "null", "foo"),
        1, 2, 3, 4, 5, 6, 7);

    // Edge case, last nano seconds of the year
    assertFileForDate(
        absFile("dev", "null", "foo", "2015", "12", "31", "23_59_59__999999999.jpg"),
        absFile("dev", "null", "foo"),
        2015, 12, 31, 23, 59, 59, 999999999);

    // Edge case, first nano second of the year.
    assertFileForDate(
        absFile("dev", "null", "foo", "2016", "01", "01", "00_00_00__000000000.jpg"),
        absFile("dev", "null", "foo"),
        2016, 1, 1, 0, 0, 0, 0);

    // Root... why not.
    assertFileForDate(
        absFile("/", "2015", "06", "27", "23_54_42__000000000.jpg"),
        absFile("/"),
        2015, 6, 27, 23, 54, 42, 0);
  }

  private void assertFileForDate(File expectedFile, File rootDir, int year, int month,
                                 int day, int hour, int minute, int second, int nanos) {
    FreeSpaceReporter mockReporter = mock(FreeSpaceReporter.class);
    when(mockReporter.isMinSpaceAvailable()).thenReturn(true);

    ImageRepository repository =
        new ImageRepository(rootDir, mockReporter, true /* this is a test */);

    LocalDate date = LocalDate.of(year, month, day);
    LocalTime time = LocalTime.of(hour, minute, second, nanos);
    LocalDateTime dateTime = LocalDateTime.of(date, time);
    File file = repository.getFileForTime(dateTime);
    Assert.assertEquals(expectedFile.getAbsolutePath(), file.getAbsolutePath());
  }

  private File absFile(String first, String... components) {
    File file = new File(first);
    for (String component : components) {
      file = new File(file, component);
    }
    return file;
  }
}
