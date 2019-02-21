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

package com.s13g.winston.tools.sauron.taker;

import com.google.common.flogger.FluentLogger;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;




import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A PictureTaker that can be used for testing, when a webcam is not accessible.
 */
public class PictureTakerTestingImpl implements PictureTaker {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  private final List<byte[]> mTestImages;
  private int mCounter = 0;

  PictureTakerTestingImpl(List<byte[]> testImages) {
    mTestImages = testImages;
  }

  @Nonnull
  @Override
  public ListenableFuture<Boolean> captureImage(File file) {
    SettableFuture<Boolean> future = SettableFuture.create();
    try (FileOutputStream out = new FileOutputStream(file);
         ByteArrayInputStream in = new ByteArrayInputStream(mTestImages.get(mCounter))) {
      long bytesWritten = ByteStreams.copy(in, out);
      future.set(bytesWritten > 0);
    } catch (IOException e) {
      log.atSevere().log("Cannot write file.", e);
      future.set(false);
    }
    if (++mCounter >= mTestImages.size()) {
      mCounter = 0;
    }
    return future;
  }

  /**
   * Loads the jpeg files from the given directory and offers them in a round-robin way.
   */
  public static PictureTaker from(File directory) {
    if (!directory.exists() || !directory.isDirectory()) {
      throw new RuntimeException("Directory does not exist: " + directory.getAbsolutePath());
    }

    File[] jpegFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
    if (jpegFiles.length == 0) {
      throw new RuntimeException("No JPEG files in directory " + directory.getAbsolutePath());
    }

    List<byte[]> files = new ArrayList<>(jpegFiles.length);
    for (File jpegFile : jpegFiles) {
      files.add(readFile(jpegFile));
    }
    return new PictureTakerTestingImpl(files);
  }

  @Nullable
  private static byte[] readFile(File file) {
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    try (FileInputStream input = new FileInputStream(file)) {
      ByteStreams.copy(input, data);
      return data.toByteArray();
    } catch (IOException e) {
      log.atWarning().log("Cannot read file. ", e);
      return null;
    }
  }
}
