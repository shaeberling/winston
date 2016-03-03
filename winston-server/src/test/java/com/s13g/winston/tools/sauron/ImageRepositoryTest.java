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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.RandomAccess;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for ImageRepository
 */
public class ImageRepositoryTest {

  @Rule
  public TemporaryFolder mImageRepoTempRoot = new TemporaryFolder();
  File mRoot;
  FakeFreeSpaceReporter mFakeFreeSpaceReporter;

  @Before
  public void initialize() throws IOException {
    mRoot = mImageRepoTempRoot.newFolder("image_repo");
    mFakeFreeSpaceReporter = new FakeFreeSpaceReporter();
  }

  @Test
  public void testFileName() throws IOException {
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

  @Test
  public void ensureDiskSpaceStaysSane() throws IOException {
    // First, let's create a temporary tree of jpeg files which the repo can use to initialize
    // itself.
    File subDir1 = new File(new File(new File(mRoot, "2016"), "03"), "01");
    assertTrue(subDir1.mkdirs());
    File subDir2 = new File(new File(new File(mRoot, "2016"), "03"), "02");
    assertTrue(subDir2.mkdirs());

    for (int i = 0; i < 23; ++i) {
      assertTrue((new File(subDir1, "File_" + i + ".jpg")).createNewFile());
    }
    for (int i = 0; i < 42; ++i) {
      assertTrue((new File(subDir2, "File_" + i + ".jpg")).createNewFile());
    }

    ImageRepository repository = new ImageRepository(mRoot, mFakeFreeSpaceReporter);
    repository.init();

    // Let's sanity check where we're starting from.
    assertThat(subDir1.listFiles()).hasLength(23);
    assertThat(subDir2.listFiles()).hasLength(42);

    // If enough space is free, no files should be deleted when a new file was added.
    mFakeFreeSpaceReporter.setFreeSpaceAvailableCountDown(0);

    createNewFileAndAddToRep("NewFile1.jpg", subDir1, repository);
    assertThat(subDir1.listFiles()).hasLength(24);
    assertThat(subDir2.listFiles()).hasLength(42);

    // Now lets pretend we ran out of space and need to delete a single file.
    mFakeFreeSpaceReporter.setFreeSpaceAvailableCountDown(1);

    createNewFileAndAddToRep("NewFile2.jpg", subDir1, repository);
    // The amount of files should not have changed.
    assertThat(subDir1.listFiles()).hasLength(24);
    assertThat(subDir2.listFiles()).hasLength(42);

    createNewFileAndAddToRep("NewFile3.jpg", subDir1, repository);
    // Since the second call to to check available space showed spaces is available, the file can
    // be added and the list of files should grow with the new file.
    assertThat(subDir1.listFiles()).hasLength(25);
    assertThat(subDir2.listFiles()).hasLength(42);

    // Now let's say something else wrote data onto the disk and there is now less space available.
    // This should make the image repo delete ten files until it starts growing the list again.
    mFakeFreeSpaceReporter.setFreeSpaceAvailableCountDown(10);

    // Let's add these to subDir2. Since the files in subDir1 we initially added are older than
    // subDir2, we should be seeing those deleted first.
    createNewFileAndAddToRep("NewFile4.jpg", subDir2, repository);
    assertThat(subDir1.listFiles()).hasLength(15);
    assertThat(subDir2.listFiles()).hasLength(43);

    createNewFileAndAddToRep("NewFile5.jpg", subDir2, repository);
    assertThat(subDir1.listFiles()).hasLength(15);
    assertThat(subDir2.listFiles()).hasLength(44);
  }

  private static void createNewFileAndAddToRep(String name, File dir, ImageRepository repository)
      throws IOException {
    File newFile = new File(dir, name);
    assertTrue(newFile.createNewFile());

    // We do the following to ensure the file exists. Before this we had issues on travis where a
    // file was written but not immediately in the list of files present.
    RandomAccessFile raFile = new RandomAccessFile(newFile, "rw");
    raFile.writeShort(42);
    FileDescriptor fd = raFile.getFD();
    raFile.close();

    repository.onFileWritten(newFile);
    // Our new file should still be there, since it's the newest. No matter whether othe files
    // had to be removed. The only case where this could happen if the disk ran out of memory
    // completely.
    assertThat(newFile.exists()).isTrue();
  }

  private void assertFileForDate(File expectedFile, File rootDir, int year, int month,
                                 int day, int hour, int minute, int second, int nanos) {
    FreeSpaceReporterImpl mockReporter = mock(FreeSpaceReporterImpl.class);
    when(mockReporter.isMinSpaceAvailable()).thenReturn(true);

    ImageRepository repository = new ImageRepository(rootDir, mockReporter);

    LocalDate date = LocalDate.of(year, month, day);
    LocalTime time = LocalTime.of(hour, minute, second, nanos);
    LocalDateTime dateTime = LocalDateTime.of(date, time);
    File file = repository.getFileForTime(dateTime);
    Assert.assertEquals(expectedFile.getAbsolutePath(), file.getAbsolutePath());
  }

  /** Create an absolute file with the given path, relative to mRoot. */
  private File absFile(String first, String... components) throws IOException {
    File file = new File(mRoot, first);
    for (String component : components) {
      file = new File(file, component);
    }
    return file;
  }

  /** Enables us to fake the state of the file system. */
  private static class FakeFreeSpaceReporter implements FreeSpaceReporter {
    // How many times isMinSpaceAvailable needs to be called to report 'true'.
    private int mCountdownToSpace = 0;

    @Override
    public boolean isMinSpaceAvailable() {
      mCountdownToSpace--;
      if (mCountdownToSpace < 0) {
        mCountdownToSpace = 0;
      }
      return mCountdownToSpace == 0;
    }

    public void setFreeSpaceAvailableCountDown(int countdownToSpace) {
      mCountdownToSpace = countdownToSpace + 1;
    }
  }
}
