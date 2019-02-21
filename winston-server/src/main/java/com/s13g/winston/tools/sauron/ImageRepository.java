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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.flogger.FluentLogger;
import com.s13g.winston.lib.core.file.Directory;
import com.s13g.winston.lib.core.file.FileWrapper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The image repository is initialize in a certain directory, which can always contain valid
 * repository data or a new directory.
 * <p>
 * Based on the current time, the repository is able to store new images into a folder structure
 * that works well for archiving and retrieval.
 */
@ParametersAreNonnullByDefault
public class ImageRepository {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  private static final String FILE_EXTENSION = ".jpg";
  private final Directory mRootDirectory;
  private final FreeSpaceReporter mFreeSpaceReporter;

  /** All image files are stored as a reference. */
  private final LinkedList<ImageRepoFile> mImageFiles;

  /**
   * Creates a new image repository.
   *
   * @param rootDirectory the root directory of the image repository
   */
  @VisibleForTesting
  ImageRepository(Directory rootDirectory, FreeSpaceReporter freeSpaceReporter) {
    mRootDirectory = rootDirectory;
    mFreeSpaceReporter = freeSpaceReporter;
    mImageFiles = new LinkedList<>();
  }

  @Nonnull
  public static ImageRepository init(long minFreeSpaceBytes, Directory rootDirectory)
      throws IOException {
    FreeSpaceReporterImpl reporter = FreeSpaceReporterImpl.from(minFreeSpaceBytes, rootDirectory
        .getPath());
    ImageRepository repository = new ImageRepository(rootDirectory, reporter);
    repository.init();
    return repository;
  }

  /**
   * Based on the current time, determines the path and file name for the image to be created.
   * <p>
   * Timestamp will be nano-second precise so avoid duplicates.
   *
   * @return A writable file that an image can be written to.
   */
  @Nonnull
  public File getCurrentFile() {
    return getFileForTime(LocalDateTime.now());
  }

  /**
   * Call this when a new image file was written to disk.
   * <p>
   * We append the new file to the list of existing files so we can access is later for e.g
   * deletion.
   * <p>
   * We also check if the number of bytes left on disk is higher or equal to the number of minimum
   * bytes allowed to be left (see constructor parameter). If the available space is too low, we
   * delete the oldest image in the list. NOTE: We do not delete images until enough space is
   * available, but only the last one. Since only one new image was added, this should result in a
   * stable system that is no more complicated than it needs to be.
   *
   * @param file the file that has just been successfully written to disk.
   * @throws IOException if the oldest file is to be deleted but deletion fails.
   */
  public void onFileWritten(FileWrapper file) throws IOException {
    mImageFiles.add(new ImageRepoFile(file));

    while (!mFreeSpaceReporter.isMinSpaceAvailable()) {
      ImageRepoFile oldestImage = mImageFiles.removeFirst();
      log.atInfo().log("Not enough space. Deleting: %d", oldestImage);
      oldestImage.delete();
    }
  }

  /**
   * Initializes the repository at the given location. Builds the list of file reference for fast
   * access and ability to delete LRU.
   */
  @VisibleForTesting
  void init() {
    Preconditions.checkState(mImageFiles.isEmpty(), "ImageRepo already initialized.");
    log.atInfo().log("Initializing ImageRepository");

    // Build list of existing image repo files.
    try {
      mRootDirectory.walkFileTree(file -> {
        if (file.toString().toLowerCase().endsWith(".jpg")) {
          mImageFiles.add(new ImageRepoFile(file));
          log.atInfo().log("INIT: Adding existing file: %s", file);
        }
      });
      log.atInfo().log("Found %d existing files.", mImageFiles.size());
      log.atInfo().log("Sorting...");
      Collections.sort(mImageFiles);
      log.atInfo().log("Sorting Done.");
    } catch (IOException | RuntimeException ex) {
      log.atSevere().withCause(ex).log("Cannot scan existing image repo.");
    }
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

    if (directory.exists() && !directory.isDirectory()) {
      throw new RuntimeException("Location is not a directory: " + directory.getAbsolutePath());
    }
    if (!directory.exists() && !directory.mkdirs()) {
      throw new RuntimeException("Cannot not create directory: " + directory.getAbsolutePath());
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

    return new File(new File(new File(mRootDirectory.getFile(), yearStr), monthStr), dayStr);
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
