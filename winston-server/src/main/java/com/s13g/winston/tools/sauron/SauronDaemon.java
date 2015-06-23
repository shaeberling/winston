/*
 * Copyright 2014 Sascha Haeberling
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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Requests, archives and serves webcam images.
 * <ol>
 * <li>Requests webcam images from the devices</li>
 * <li>Archives the images in a folder structure</li>
 * <li>Serves the images to web browsers</li>
 * </ol>
 */
public class SauronDaemon {
  private static Logger LOG = LogManager.getLogger(SauronDaemon.class);


  public static void main(String[] args) {
    // The commands have to be executed serially, so use a single-thread executor for fetching
    // the images from the webcam device.
    final ExecutorService cameraCommandExecutor = Executors.newSingleThreadExecutor();

    // TODO: Just for testing.
    final File capturedFile = new File("sauron2.jpg");
    ListenableFuture<Boolean> captureResult =
        (new PictureTaker(cameraCommandExecutor)).captureImage(capturedFile.getAbsolutePath());
    Futures.addCallback(captureResult, new FutureCallback<Boolean>() {
      @Override
      public void onSuccess(Boolean result) {
        if (!result) {
          LOG.warn("Taking picture not successful");
          return;
        }
        onPictureTaken(capturedFile);

        // TODO: Shutting down after one successful file, just for testing.
        cameraCommandExecutor.shutdown();
      }

      @Override
      public void onFailure(Throwable t) {
        LOG.warn("Taking picture failed", t);
      }
    });
  }

  public static void onPictureTaken(File imageFile) {
    LOG.info("Image successfully stored: " + imageFile.getAbsolutePath());
  }
}
