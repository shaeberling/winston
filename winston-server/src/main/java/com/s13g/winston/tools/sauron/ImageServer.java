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

import com.s13g.winston.common.io.DataLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executor;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.GuardedBy;

/**
 * Simple web server to serve the webcam pictures.
 */
@ParametersAreNonnullByDefault
public class ImageServer {

  private static final Logger LOG = LogManager.getLogger(ImageServer.class);

  /** Used to read the file into memory to serve it. */
  private final Executor mFileReadExecutor;
  /** The bytes of the current image to serve. */
  @GuardedBy("mBytesLock")
  private byte[] mCurrentImageBytes;
  /** Locks access on the mCurrentImageBytes object */
  private final Object mBytesLock;

  /**
   * Creates a new image server.
   *
   * @param fileReadExecutor the executor to use to read new image files into memory.
   */
  public ImageServer(Executor fileReadExecutor) {
    mFileReadExecutor = fileReadExecutor;
    mCurrentImageBytes = new byte[0];
    mBytesLock = new Object();
  }

  /**
   * Set the current image file. This will make the file at the given location being loaded into
   * memory from where it will be served when requested. Setting a new current file will override
   * the previous one.
   *
   * @param currentImageLoader loads the current image file.
   */
  void setCurrentFile(DataLoader currentImageLoader) {
    mFileReadExecutor.execute(() -> {
      synchronized (mBytesLock) {
        Optional<byte[]> currentImage = currentImageLoader.load();
        if (currentImage.isPresent()) {
          mCurrentImageBytes = currentImage.get();
        } else {
          LOG.error("Could not load current image. Not updating.");
        }
      }
    });
  }

  /** Serves the current image file to the given response. */
  void serveCurrentFile(Response response) throws IOException {
    synchronized (mBytesLock) {
      serveData("image/jpeg", mCurrentImageBytes, response);
    }
  }

  /** Serves the given data and content type to the given response. */
  private void serveData(String contentType, byte[] data, Response response) throws IOException {
    response.setStatus(Status.OK);
    response.setContentType(contentType);
    response.getOutputStream().write(data);
    response.getOutputStream().close();
  }
}
