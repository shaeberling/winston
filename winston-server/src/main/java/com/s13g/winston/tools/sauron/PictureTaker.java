/*
 * Copyright 2015 Sascha Haeberling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.s13g.winston.tools.sauron;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Accesses the webcam and produces images using fswebcam.
 */
@ParametersAreNonnullByDefault
public class PictureTaker {
  private static Logger LOG = LogManager.getLogger(PictureTaker.class);
  /**
   * The command to execute to capture an image.
   * Note: Add '--no-banner' to remove the timestamp banner at the bottom of the image file.
   */
  private static final String COMMAND = "/usr/bin/fswebcam -r 1920x1080 %s";
  private final Executor mExecutor;

  public PictureTaker(Executor executor) {
    mExecutor = executor;
  }

  /**
   * Captures an image and writes it to the give file.
   *
   * @param file the path to which to write the final image file to.
   */
  @Nonnull
  public ListenableFuture<Boolean> captureImage(File file) {
    SettableFuture<Boolean> result = SettableFuture.create();
    try {
      Process process = new ProcessBuilder(createCommandForFileName(file.getAbsolutePath()))
          .redirectErrorStream(true).start();
      handleProcess(process, result);
    } catch (IOException e) {
      LOG.error("Could not capture image", e);
      result.setException(e);
    }
    return result;
  }

  /**
   * Asynchronously waits to the process to finish and informs the given future.
   *
   * @param process the process to wait for to end.
   * @param result  the future to set when the process is done.
   */
  private void handleProcess(final Process process, final SettableFuture<Boolean> result) {
    mExecutor.execute(() -> {
      try {
        int returnValue = process.waitFor();
        result.set(returnValue == 0);
      } catch (InterruptedException e) {
        LOG.error("Interrupted while capturing image", e);
        result.set(false);
      }
    });
  }

  @Nonnull
  private String[] createCommandForFileName(String fileName) {
    return String.format(COMMAND, fileName).split("\\s+");
  }
}
