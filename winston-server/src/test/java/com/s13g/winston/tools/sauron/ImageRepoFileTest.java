/*
 * Copyright 2016 The Winston Authors
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

import com.s13g.winston.lib.core.util.file.FileWrapper;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ImageRepoFile}.
 */
public class ImageRepoFileTest {
  private FileWrapper mPath;
  private ImageRepoFile mFile;
  private BasicFileAttributes mAttributes;

  @Before
  public void initialize() throws IOException {
    mPath = mock(FileWrapper.class);
    mAttributes = mock(BasicFileAttributes.class);
    mFile = new ImageRepoFile(mPath);
    when(mPath.readBasicAttributes()).thenReturn(mAttributes);
  }

  @Test
  public void testDeleteFailsNoRegularFile() throws IOException {
    when(mPath.isRegularFile()).thenReturn(false);
    when(mPath.deleteIfExists()).thenReturn(true);
    try {
      mFile.delete();
      fail("Delete should throw an exception. Not a regular file.");
    } catch (IOException expected) {
      // Expected.
    }
  }

  @Test
  public void testDeleteFails() throws IOException {
    when(mPath.isRegularFile()).thenReturn(true);
    when(mPath.deleteIfExists()).thenReturn(false);
    try {
      mFile.delete();
      fail("Delete should throw an exception since it failed.");
    } catch (IOException expected) {
      // Expected.
    }
  }

  @Test
  public void testDeleteSucceeds() throws IOException {
    when(mPath.isRegularFile()).thenReturn(true);
    when(mPath.deleteIfExists()).thenReturn(true);

    try {
      mFile.delete();
    } catch (Throwable t) {
      fail("Delete should succeed");
    }
  }

  @Test
  public void testCompareToFailWithNull() {
    assertEquals(-1, mFile.compareTo(null));
  }

  @Test
  public void testCompareToSameCreationTime() throws IOException {
    ImageRepoFile otherFile = createFileWithCreationTime(424242);
    when(mAttributes.creationTime()).thenReturn(FileTime.fromMillis(424242));
    assertEquals(0, mFile.compareTo(otherFile));
  }

  @Test
  public void testCompareToDifferentCreationTime1() throws IOException {
    ImageRepoFile otherFile = createFileWithCreationTime(424243);
    when(mAttributes.creationTime()).thenReturn(FileTime.fromMillis(424242));
    assertEquals(-1, mFile.compareTo(otherFile));
  }

  @Test
  public void testCompareToDifferentCreationTime2() throws IOException {
    ImageRepoFile otherFile = createFileWithCreationTime(424241);
    when(mAttributes.creationTime()).thenReturn(FileTime.fromMillis(424242));
    assertEquals(1, mFile.compareTo(otherFile));
  }

  @Test
  public void testCompareToFailsDueToException() throws IOException {
    ImageRepoFile otherFile = createFileWithCreationTime(424242);
    when(mPath.readBasicAttributes()).thenThrow(new IOException("Boom!"));

    try {
      assertEquals(1, mFile.compareTo(otherFile));
      fail("Should have thrown due to file level exception.");
    } catch (RuntimeException expected) {
      // Expected.
    }
  }

  private static ImageRepoFile createFileWithCreationTime(int creationTimeMillis) throws
      IOException {
    BasicFileAttributes otherAttributes = mock(BasicFileAttributes.class);
    when(otherAttributes.creationTime()).thenReturn(FileTime.fromMillis(creationTimeMillis));

    FileWrapper otherPath = mock(FileWrapper.class);
    when(otherPath.readBasicAttributes()).thenReturn(otherAttributes);

    return new ImageRepoFile(otherPath);
  }
}
