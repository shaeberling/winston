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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;

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
  private static final Logger LOG = LogManager.getLogger(ImageRepository.class);
  private static final String FILE_EXTENSION = ".jpg";
  private final File mRootDirectory;
  private final FreeSpaceReporter mFreeSpaceReporter;

  /** All image files are stored as a reference. */
  private final LinkedList<ImageRepoFile> mImageFiles;

  /**
   * Creates a new image repository.
   *
   * @param rootDirectory the root directory of the image repository
   */
  @VisibleForTesting
  ImageRepository(File rootDirectory, FreeSpaceReporter freeSpaceReporter) {
    mRootDirectory = rootDirectory;
    mFreeSpaceReporter = freeSpaceReporter;
    mImageFiles = new LinkedList<>();
  }

  @Nonnull
  public static ImageRepository init(long minFreeSpaceBytes, File rootDirectory)
      throws IOException {
    FreeSpaceReporterImpl reporter = FreeSpaceReporterImpl.from(minFreeSpaceBytes, rootDirectory.toPath());
    ImageRepository repository = new ImageRepository(rootDirectory, reporter);
    repository.init();
    return repository;
  }

  /**
   * Based on the current time, determines the path and file name for the image to be created.
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
   * Call this when a new image file was written to disk.
   * <p/>
   * We append the new file to the list of existing files so we can access is later for e.g
   * deletion.
   * <p/>
   * We also check if the number of bytes left on disk is higher or equal to the number of minimum
   * bytes allowed to be left (see constructor parameter). If the available space is too low, we
   * delete the oldest image in the list. NOTE: We do not delete images until enough space is
   * available, but only the last one. Since only one new image was added, this should result in a
   * stable system that is no more complicated than it needs to be.
   *
   * @param file the file that has just been successfully written to disk.
   * @throws IOException if the oldest file is to be deleted but deletion fails.
   */
  public void onFileWritten(File file) throws IOException {
    mImageFiles.add(new ImageRepoFile(file.toPath()));

    if (!mFreeSpaceReporter.isMinSpaceAvailable()) {
      ImageRepoFile oldestImage = mImageFiles.removeFirst();
      LOG.info("Not enough space. Deleting: " + oldestImage);
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
    LOG.info("Initializing ImageRepository");

    if (!mRootDirectory.exists() || !mRootDirectory.isDirectory()) {
      LOG.info("Image repository does not exist or is not a directory: " + mRootDirectory
          .getAbsolutePath());
      return;
    }

    // Build list of existing image repo files.
    try {
      Files.walkFileTree(mRootDirectory.toPath(), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if (file.toString().toLowerCase().endsWith(".jpg")) {
            mImageFiles.add(new ImageRepoFile(file));
            LOG.info("INIT: Adding existing file: " + file);
          }
          return FileVisitResult.CONTINUE;
        }
      });
      LOG.info("Found " + mImageFiles.size() + " existing files.");
      LOG.info("Sorting...");
      Collections.sort(mImageFiles);
      LOG.info("Sorting Done.");
    } catch (IOException | RuntimeException ex) {
      LOG.error("Cannot scan existing image repo.", ex);
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
