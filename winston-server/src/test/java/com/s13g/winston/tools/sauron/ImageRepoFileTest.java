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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ImageRepoFile}.
 */
public class ImageRepoFileTest {
  private FileWrapper mPath;
  private ImageRepoFile mFile;

  @Before
  public void initialize() {
    mPath = mock(FileWrapper.class);
    mFile = new ImageRepoFile(mPath);
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

  // TODO: Add tests for compareTo.
}
