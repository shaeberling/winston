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

import com.s13g.winston.lib.core.file.Directory;
import com.s13g.winston.lib.core.file.FileWrapper;
import com.s13g.winston.lib.core.file.SimpleFileVisitor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for ImageRepository
 */
public class ImageRepositoryTest {

  @Rule
  public TemporaryFolder mImageRepoTempRoot = new TemporaryFolder();
  FakeFreeSpaceReporter mFakeFreeSpaceReporter;

  Directory mMockRootDir;

  @Before
  public void initialize() throws IOException {
    mFakeFreeSpaceReporter = new FakeFreeSpaceReporter();
    mMockRootDir = mock(Directory.class);
  }

  @Test
  public void testFileName() throws IOException {
    File root = mImageRepoTempRoot.newFolder("foo");
    // Time this test was written. Cheers!
    assertFileForDate(
        absFile(root, "dev", "null", "foo", "2015", "06", "27", "23_54_42__000000000.jpg"),
        absFile(root, "dev", "null", "foo"),
        2015, 6, 27, 23, 54, 42, 0);

    // Make sure padding is right everywhere.
    assertFileForDate(
        absFile(root, "dev", "null", "foo", "0001", "02", "03", "04_05_06__000000007.jpg"),
        absFile(root, "dev", "null", "foo"),
        1, 2, 3, 4, 5, 6, 7);

    // Edge case, last nano seconds of the year
    assertFileForDate(
        absFile(root, "dev", "null", "foo", "2015", "12", "31", "23_59_59__999999999.jpg"),
        absFile(root, "dev", "null", "foo"),
        2015, 12, 31, 23, 59, 59, 999999999);

    // Edge case, first nano second of the year.
    assertFileForDate(
        absFile(root, "dev", "null", "foo", "2016", "01", "01", "00_00_00__000000000.jpg"),
        absFile(root, "dev", "null", "foo"),
        2016, 1, 1, 0, 0, 0, 0);

    // Root... why not.
    assertFileForDate(
        absFile(root, "/", "2015", "06", "27", "23_54_42__000000000.jpg"),
        absFile(root, "/"),
        2015, 6, 27, 23, 54, 42, 0);
  }

  @Test
  public void ensureDiskSpaceStaysSane() throws IOException {
    // First, let's create a temporary tree of jpeg files which the repo can use to initialize
    // itself.
//    File subDir1 = new File(new File(new File(mRoot.getFile(), "2016"), "03"), "01");
//    assertTrue(subDir1.mkdirs());
//    File subDir2 = new File(new File(new File(mRoot.getFile(), "2016"), "03"), "02");
//    assertTrue(subDir2.mkdirs());
//
//    for (int i = 0; i < 23; ++i) {
//      assertTrue((new File(subDir1, "File_" + i + ".jpg")).createNewFile());
//    }
//    for (int i = 0; i < 7; ++i) {
//      assertTrue((new File(subDir2, "File_" + i + ".jpg")).createNewFile());
//    }

    List<FileWrapper> filesInRepo = new ArrayList<>();

    doAnswer(invocation -> {
      SimpleFileVisitor visitor = (SimpleFileVisitor) invocation.getArguments()[0];
      for (int i = 0; i < 42; ++i) {
        FileWrapper fileInRepo = createdMockedFile("File_" + i + ".jpg", 1000 + i);
        visitor.visitFile(fileInRepo);
        filesInRepo.add(fileInRepo);
      }
      return null;
    }).when(mMockRootDir).walkFileTree(any(SimpleFileVisitor.class));


    ImageRepository repository = new ImageRepository(mMockRootDir, mFakeFreeSpaceReporter);
    repository.init();

    // If enough space is free, no files should be deleted when a new file was added.
    mFakeFreeSpaceReporter.setFreeSpaceAvailableCountDown(0);
    FileWrapper newFile = createdMockedFile("File_1000.jpg", 2000);
    repository.onFileWritten(newFile);
    filesInRepo.add(newFile);

    // No file should be deleted
    for (FileWrapper file : filesInRepo) {
      verify(file, never()).deleteIfExists();
    }

    // Now lets pretend we ran out of space and need to delete a single file.
    mFakeFreeSpaceReporter.setFreeSpaceAvailableCountDown(1);
    newFile = createdMockedFile("File_1001.jpg", 2001);
    repository.onFileWritten(newFile);
    filesInRepo.add(newFile);

    // The oldest file should be deleted, not the others.
    verify(filesInRepo.remove(0)).deleteIfExists();
    for (FileWrapper file : filesInRepo) {
      verify(file, never()).deleteIfExists();
    }

    newFile = createdMockedFile("File_1002.jpg", 2002);
    repository.onFileWritten(newFile);
    filesInRepo.add(newFile);
    for (FileWrapper file : filesInRepo) {
      verify(file, never()).deleteIfExists();
    }

    // Now let's say something else wrote data onto the disk and there is now less space available.
    // This should make the image repo delete ten files until it starts growing the list again.
    mFakeFreeSpaceReporter.setFreeSpaceAvailableCountDown(15);

    newFile = createdMockedFile("File_1003.jpg", 2003);
    repository.onFileWritten(newFile);
    filesInRepo.add(newFile);

    for (int i = 0; i < 15; ++i) {
      verify(filesInRepo.remove(0)).deleteIfExists();
    }
    for (FileWrapper file : filesInRepo) {
      verify(file, never()).deleteIfExists();
    }

    // And finally, since enough space is available, we should be able to add three new files
    // without further deletions.

    newFile = createdMockedFile("File_1004.jpg", 2004);
    repository.onFileWritten(newFile);
    filesInRepo.add(newFile);
    newFile = createdMockedFile("File_1005.jpg", 2005);
    repository.onFileWritten(newFile);
    filesInRepo.add(newFile);
    newFile = createdMockedFile("File_1006.jpg", 2006);
    repository.onFileWritten(newFile);
    filesInRepo.add(newFile);
    for (FileWrapper file : filesInRepo) {
      verify(file, never()).deleteIfExists();
    }
  }

  private static FileWrapper createdMockedFile(String name, long createdMillis) throws IOException {
    BasicFileAttributes attributes = mock(BasicFileAttributes.class);
    when(attributes.creationTime()).thenReturn(FileTime.fromMillis(createdMillis));

    FileWrapper file = mock(FileWrapper.class);
    when(file.toString()).thenReturn(name);
    when(file.readBasicAttributes()).thenReturn(attributes);
    when(file.isRegularFile()).thenReturn(true);
    when(file.deleteIfExists()).thenReturn(true);
    return file;
  }

  private static int getFileCount(File directory) throws IOException {
    int count = 0;
    DirectoryStream<Path> fileStream = Files.newDirectoryStream(directory.toPath());

    for (Path file : fileStream) {
      String name = file.getFileName().toString();
      if (Files.isRegularFile(file) && name.startsWith("File") && name.endsWith(".jpg")) {
        count++;
      }
    }
    return count;
  }

  private void assertFileForDate(File expectedFile, File rootDir, int year, int month,
                                 int day, int hour, int minute, int second, int nanos) {
    FreeSpaceReporterImpl mockReporter = mock(FreeSpaceReporterImpl.class);
    when(mockReporter.isMinSpaceAvailable()).thenReturn(true);
    Directory mockDirectory = mock(Directory.class);
    when(mockDirectory.getFile()).thenReturn(rootDir);

    ImageRepository repository = new ImageRepository(mockDirectory, mockReporter);

    LocalDate date = LocalDate.of(year, month, day);
    LocalTime time = LocalTime.of(hour, minute, second, nanos);
    LocalDateTime dateTime = LocalDateTime.of(date, time);
    File file = repository.getFileForTime(dateTime);
    assertEquals(expectedFile.getAbsolutePath(), file.getAbsolutePath());
  }

  /** Create an absolute file with the given path, relative to mRoot. */
  private File absFile(File folder, String first, String... components) throws IOException {
    File file = new File(folder, first);
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
