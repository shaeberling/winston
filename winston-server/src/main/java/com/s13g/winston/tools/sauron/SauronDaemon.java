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

import com.s13g.winston.common.ContainerServer;
import com.s13g.winston.common.io.FileDataLoader;
import com.s13g.winston.common.io.ResourceLoader;
import com.s13g.winston.common.io.ResourceLoaderImpl;
import com.s13g.winston.lib.core.file.DirectoryImpl;
import com.s13g.winston.lib.core.file.FileWrapperImpl;
import com.s13g.winston.tools.sauron.taker.PictureTaker;
import com.s13g.winston.tools.sauron.taker.PictureTakerImpl;
import com.s13g.winston.tools.sauron.taker.PictureTakerTestingImpl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Requests, archives and serves webcam images. <ol> <li>Requests webcam images from the
 * devices</li> <li>Archives the images in a folder structure</li> <li>Serves the images to web
 * browsers</li> </ol>
 */
@ParametersAreNonnullByDefault
public class SauronDaemon {
  private static Logger LOG = LogManager.getLogger(SauronDaemon.class);
  private static final boolean EMULATE_CAMERA = false;
  /* Note: Set this when you do testing. */
  private static final String TEST_PICTURES_ROOT = "/dev/null";

  private static final String REPOSITORY_ROOT = "/home/pi/image_repo";

  private static final int SHOT_DELAY_MILLIS = 10000; // Once every 10 seconds.
  private static final int HTTP_PORT = 1986;
  private static final long MIN_BYTES_AVAILABLE = 500L * 1000L * 1000L; // 100 MB

  public static void main(String[] args) throws IOException {
    ExecutorService cameraCommandExecutor = Executors.newSingleThreadExecutor();
    ScheduledExecutorService schedulerExecutor = Executors.newSingleThreadScheduledExecutor();
    ExecutorService fileReadingExecutor = Executors.newSingleThreadExecutor();

    PictureTaker pictureTaker = EMULATE_CAMERA ?
        PictureTakerTestingImpl.from(new File(TEST_PICTURES_ROOT))
        : new PictureTakerImpl(cameraCommandExecutor);
    final ImageRepository imageRepository =
        ImageRepository.init(MIN_BYTES_AVAILABLE, DirectoryImpl.create(REPOSITORY_ROOT));
    ContainerServer.Creator serverCreator = ContainerServer.getDefaultCreator();
    ResourceLoader resourceLoader = new ResourceLoaderImpl();
    final ImageServer imageServer = new ImageServer(fileReadingExecutor);
    final WebRequestContainer requestContainer = new WebRequestContainer(HTTP_PORT,
        serverCreator, imageServer, resourceLoader);
    Scheduler scheduler = new Scheduler(pictureTaker, imageRepository, schedulerExecutor);

    // Start web server for serving data.
    requestContainer.start();

    // Start the scheduler to take pictures.
    scheduler.start(SHOT_DELAY_MILLIS, (pictureFile) -> {
      LOG.debug("New picture available: " + pictureFile.getAbsolutePath());
      try {
        imageRepository.onFileWritten(new FileWrapperImpl(pictureFile.toPath()));
      } catch (IOException ex) {
        // TODO: We should probably kill the daemon to prevent a system out of memory condition.
        LOG.error("Cannot delete oldest file. System might run out of memory soon.", ex);
      }
      imageServer.updateCurrentFile(new FileDataLoader(pictureFile));
    });
  }
}
