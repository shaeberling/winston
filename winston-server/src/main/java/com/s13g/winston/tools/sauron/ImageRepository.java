/*
 * Copyright 2015 Sascha Haeberling
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
 *
 */

package com.s13g.winston.tools.sauron;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.io.File;
import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The image repository is initialize in a certain directory, which can always contain valid
 * repository data or a new directory.
 * <p/>
 * Based on the current time, the repository is able to store new images into a folder structure
 * that works well for archiving and retrieval.
 */
@ParametersAreNonnullByDefault
public class ImageRepository {
  private static final String FILE_EXTENSION = ".jpg";
  private final File mRootDirectory;
  private final boolean mDontCreateDirs;

  /**
   * Creates a new image repository.
   *
   * @param rootDirectory  the root directory of the image repository
   * @param dontCreateDirs set this to 'true' in tests to avoid it creating directories.
   */
  @VisibleForTesting
  ImageRepository(File rootDirectory, boolean dontCreateDirs) {
    mRootDirectory = rootDirectory;
    mDontCreateDirs = dontCreateDirs;
  }

  @Nonnull
  public static ImageRepository init(File rootDirectory) {
    ImageRepository repository =
        new ImageRepository(rootDirectory, false /** this is not a test */);
    repository.init();
    return repository;
  }

  /**
   * Based on the current time, determines the path and file name for the image to be
   * created.
   * <p/>
   * Timestamp will be nano-second precise so avoid duplicates.
   *
   * @return A writable file that an image can be written to.
   */
  @Nonnull
  public File getCurrentFile() {
    return getFileForTime(LocalDateTime.now());
  }

  /**
   * Initializes the repository at the given location.
   */
  private void init() {
    // TODO: Initialize a data structure with all existing files for fast access.
  }

  /**
   * See {@link }#getCurrentFile}.
   *
   * @param time the time for which to generate the path and file name.
   * @return A writable file that an image can be written to.
   */
  @Nonnull
  @VisibleForTesting
  File getFileForTime(LocalDateTime time) {
    File directory = getDirectory(time);

    // We don't want to run these checks in fast running tests.
    if (!mDontCreateDirs) {
      if (directory.exists() && !directory.isDirectory()) {
        throw new RuntimeException("Location is not a directory: " + directory.getAbsolutePath());
      }
      if (!directory.exists() && !directory.mkdirs()) {
        throw new RuntimeException("Cannot not create directory: " + directory.getAbsolutePath());
      }
    }
    return new File(directory, getFilename(time));
  }

  private File getDirectory(LocalDateTime time) {
    int year = time.getYear();
    int month = time.getMonthValue();
    int day = time.getDayOfMonth();

    // Just double checking that documentation says the right thing ;)
    Preconditions.checkState(year >= 1);
    Preconditions.checkState(month >= 1 && month <= 12);
    Preconditions.checkState(day >= 1 && month <= 31);

    String yearStr = String.format("%04d", year);
    String monthStr = String.format("%02d", month);
    String dayStr = String.format("%02d", day);

    return new File(new File(new File(mRootDirectory, yearStr), monthStr), dayStr);
  }

  private String getFilename(LocalDateTime time) {
    StringBuilder filename = new StringBuilder();
    filename.append(String.format("%02d", time.getHour()));
    filename.append("_");
    filename.append(String.format("%02d", time.getMinute()));
    filename.append("_");
    filename.append(String.format("%02d", time.getSecond()));
    filename.append("__");
    filename.append(String.format("%09d", time.getNano()));
    filename.append(FILE_EXTENSION);
    return filename.toString();
  }

}
