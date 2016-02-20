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

import com.google.common.base.Preconditions;
import com.s13g.winston.lib.core.util.file.FileWrapper;
import com.s13g.winston.lib.core.util.file.FileWrapperImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * An image file created by Sauron
 */
public class ImageRepoFile implements Comparable<ImageRepoFile> {
  private final FileWrapper mFile;

  public ImageRepoFile(FileWrapper file) {
    mFile = file;
  }

  ImageRepoFile(Path file) {
    this(new FileWrapperImpl(file));
  }

  /**
   * Deletes the given file.
   *
   * @throws IOException thrown if the file could not be deleted.
   */
  public void delete() throws IOException {
    if (!mFile.isRegularFile()) {
      throw new IOException("Not a regular file: " + mFile);
    }

    try {
      if (!mFile.deleteIfExists()) {
        throw new IOException("File does not exist: " + mFile);
      }
    } catch (IOException | SecurityException ex) {
      throw new IOException("Cannot delete image repo file.", ex);
    }
  }

  @Override
  public int compareTo(ImageRepoFile other) {
    if (other == null || other.mFile == null) {
      return -1;
    }
    if (mFile == null) {
      return 1;
    }

    FileTime otherCreationTime;
    FileTime thisCreationTime;
    try {
      BasicFileAttributes attributes = other.mFile.readBasicAttributes();
      otherCreationTime = attributes.creationTime();
      attributes = mFile.readBasicAttributes();
      thisCreationTime = attributes.creationTime();
    } catch (IOException ex) {
      throw new RuntimeException("Error getting file attributes for sorting.", ex);
    }
    Preconditions.checkNotNull(otherCreationTime, "OtherCreationTime must be present.");
    Preconditions.checkNotNull(thisCreationTime, "ThisCreationTime must be present.");

    // Sort in ascending order.
    return thisCreationTime.compareTo(otherCreationTime);
  }

  @Override
  public String toString() {
    return mFile.toString();
  }
}
