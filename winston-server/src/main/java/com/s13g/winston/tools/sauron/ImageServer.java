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
import java.io.OutputStream;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

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
  private final LinkedBlockingQueue<byte[]> mmJpegQueue = new LinkedBlockingQueue<>(1);

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
   * Loads a new image from the load into memory from where it will be served when requested.
   * Setting a new current file will override the previous one.
   *
   * @param currentImageLoader loads the current image file.
   */
  void updateCurrentFile(DataLoader currentImageLoader) {
    mFileReadExecutor.execute(() -> {
      synchronized (mBytesLock) {
        Optional<byte[]> currentImage = currentImageLoader.load();
        if (currentImage.isPresent()) {
          mCurrentImageBytes = currentImage.get();
          if (!mmJpegQueue.offer(mCurrentImageBytes)) {
            LOG.warn("Cannot add new image to mMjpegQueue.");
          }
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

  void serveMotionJpegAsync(final Response response) throws IOException {
    LOG.info("Serving MJPEG...");
    response.setContentType("multipart/x-mixed-replace;boundary=ipcamera");
    response.setStatus(Status.OK);

    OutputStream outputStream = response.getOutputStream();
    Executor mMjpegExecutor = Executors.newSingleThreadExecutor();
    mMjpegExecutor.execute(() -> {
      byte[] jpegData = null;
      try {
        // Immediately serve most recent frame.
        synchronized (mBytesLock) {
          serveMotionJpegFrame(mCurrentImageBytes, outputStream);
        }

        // Skip the first frame, since it's old. Then continue to read updated frames.
        mmJpegQueue.take();
        while ((jpegData = mmJpegQueue.take()) != null) {
          serveMotionJpegFrame(jpegData, outputStream);
        }
      } catch (IOException | InterruptedException e) {
        LOG.warn("Interrupted while serving MJPEG data. Exiting MJPEG loop.");
        try {
          response.close();
        } catch (IOException ignore) {
        }
      }
    });
  }

  private void serveMotionJpegFrame(byte[] jpegData, OutputStream outputStream) throws IOException {
    outputStream.write("--ipcamera\r\n".getBytes());
    outputStream.write("Content-Type: image/jpeg\r\n".getBytes());
    outputStream.write(("Content-Length: " + jpegData.length + "\r\n\r\n").getBytes());
    outputStream.write(jpegData);
    LOG.debug("Write mJpeg frame");
  }

  /** Serves the given data and content type to the given response. */
  private void serveData(String contentType, byte[] data, Response response) throws IOException {
    response.setStatus(Status.OK);
    response.setContentType(contentType);
    response.getOutputStream().write(data);
    response.getOutputStream().close();
  }
}
