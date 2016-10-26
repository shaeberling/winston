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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.s13g.winston.tools.sauron.taker.PictureTaker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Drives the picture taker to take repeated pictures.
 */
@ParametersAreNonnullByDefault
public class Scheduler {
  /** Classes implementing this interface can be informed when a new picture is ready. */
  interface Listener {
    /**
     * Called when a new picture is available.
     *
     * @param pictureFile the file location of the new picture.
     */
    void onPictureReady(File pictureFile);
  }

  private static Logger LOG = LogManager.getLogger(Scheduler.class);
  private final PictureTaker mPictureTaker;
  private final ImageRepository mImageRepository;
  private ScheduledExecutorService mExecutor;

  Scheduler(PictureTaker pictureTaker, ImageRepository imageRepository,
            ScheduledExecutorService executor) {
    mPictureTaker = pictureTaker;
    mImageRepository = imageRepository;
    mExecutor = executor;
  }

  /**
   * Start the picture taking scheduler.
   *
   * @param delayMillis how much time should pass in between shots (in milliseconds).
   * @param listener this listener is called when a new image has been captured. Don't do a lot of
   * work here since it might delay the next shot.
   */
  void start(int delayMillis, final Listener listener) {
    mExecutor.scheduleAtFixedRate(() -> {
      final File nextImageFile = mImageRepository.getCurrentFile();
      ListenableFuture<Boolean> captureResult = mPictureTaker.captureImage(nextImageFile);
      Futures.addCallback(captureResult, new FutureCallback<Boolean>() {
        @Override
        public void onSuccess(@Nullable Boolean result) {
          if (result == null || !result) {
            LOG.warn("Taking picture not successful");
            return;
          }
          // TODO: We might want to do ths on a different executor so that a long-running
          // listener is not blocking the taking queue.
          listener.onPictureReady(nextImageFile);
        }

        @Override
        public void onFailure(Throwable t) {
          LOG.warn("Taking picture failed", t);
        }
      });
    }, 0 /* No initial delay */, delayMillis, TimeUnit.MILLISECONDS);
  }

  /** Shuts down the executor and thus stops scheduler. */
  public void stop() {
    mExecutor.shutdown();
  }
}
