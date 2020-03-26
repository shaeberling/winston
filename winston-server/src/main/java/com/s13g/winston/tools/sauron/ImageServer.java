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

import com.google.common.collect.ImmutableList;
import com.google.common.flogger.FluentLogger;
import com.google.common.io.MoreFiles;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.s13g.winston.common.io.DataLoader;

import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.GuardedBy;

/** Simple web server to serve the webcam pictures. */
@ParametersAreNonnullByDefault
public class ImageServer {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  /** Used to read the file into memory to serve it. */
  private final Executor mFileReadExecutor;
  /** Serves the responses to the incoming requests. */
  private final ListeningExecutorService mServeMjpegExecutor;
  /** The bytes of the current image to serve. */
  @GuardedBy("mBytesLock")
  private byte[] mCurrentImageBytes;
  /** Locks access on the mCurrentImageBytes object */
  private final Object mBytesLock;

  private final LinkedBlockingQueue<byte[]> mmJpegQueue = new LinkedBlockingQueue<>(1);

  private final Set<Response> mActiveMjpegResponses = new HashSet<>();

  /**
   * Creates a new image server.
   *
   * @param fileReadExecutor the executor to use to read new image files into memory.
   */
  public ImageServer(Executor fileReadExecutor) {
    mFileReadExecutor = fileReadExecutor;
    mServeMjpegExecutor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    mCurrentImageBytes = new byte[0];
    mBytesLock = new Object();
    startServing();
  }

  /** For testing. */
  ImageServer(Executor fileReadExecutor, ExecutorService serveMjpegExecutor) {
    mFileReadExecutor = fileReadExecutor;
    mServeMjpegExecutor = MoreExecutors.listeningDecorator(serveMjpegExecutor);
    mCurrentImageBytes = new byte[0];
    mBytesLock = new Object();
    startServing();
  }

  /**
   * Loads a new image from the load into memory from where it will be served when requested.
   * Setting a new current file will override the previous one.
   *
   * @param currentImageLoader loads the current image file.
   */
  void updateCurrentFile(DataLoader currentImageLoader) {
    mFileReadExecutor.execute(
        () -> {
          Optional<byte[]> currentImage = currentImageLoader.load();
          if (currentImage.isPresent()) {
            synchronized (mBytesLock) {
              mCurrentImageBytes = currentImage.get();
              log.atInfo().log("Offering new mJPEG to mJPEG queue.");
              if (!mmJpegQueue.offer(mCurrentImageBytes)) {
                log.atWarning().log("Cannot add new image to mJPEG queue.");
              }
            }
          } else {
            log.atSevere().log("Could not load current image. Not updating.");
          }
        });
  }

  /** Serves the current image file to the given response. */
  void serveCurrentFile(Response response) throws IOException {
    synchronized (mBytesLock) {
      serveData("image/jpeg", mCurrentImageBytes, response);
    }
  }

  private void startServing() {
    // One master thread that loops until interrupted.
    Executors.newSingleThreadExecutor()
        .execute(
            () -> {
              while (true) {
                try {
                  // Get the data to serve to all outstanding requests.
                  byte[] jpegData = mmJpegQueue.take();
                  Set<ListenableFuture<?>> servingFutures = new HashSet<>();
                  for (Response response : ImmutableList.copyOf(mActiveMjpegResponses)) {
                    // Fires up a thread per active response being served.
                    ListenableFuture<?> future =
                        mServeMjpegExecutor.submit(() -> serveMotionJpeg(response, jpegData));
                    servingFutures.add(future);
                  }
                  // Wait for all serves to complete.
                  Futures.allAsList(servingFutures).get();
                } catch (InterruptedException | ExecutionException ex) {
                  log.atInfo().log("Got interrupted while retrieving from mJpeg queue.");
                  break;
                }
              }
            });
  }

  void startServingMjpegTo(final Response response) {
    response.setContentType("multipart/x-mixed-replace;boundary=ipcamera");
    response.setStatus(Status.OK);

    // Immediately serve the most current frame.
    synchronized (mBytesLock) {
      try {
        serveMotionJpegFrame(mCurrentImageBytes, response.getOutputStream());
      } catch (IOException ignore) {
      }
    }

    mActiveMjpegResponses.add(response);
    log.atInfo().log("Added active mJPEG response. Total now %d.", mActiveMjpegResponses.size());
  }

  private void serveMotionJpeg(final Response response, byte[] jpegData) {
    try {
      OutputStream outputStream = response.getOutputStream();

      // When we get an IOException trying to write out data, at which point we end
      // serving data as the request has likely been cancelled, we remove the response so it is
      // no longer being served.
      serveMotionJpegFrame(jpegData, outputStream);
    } catch (IOException ex) {
      mActiveMjpegResponses.remove(response);
      log.atInfo().log("Removing inactive response. Total now %d.", mActiveMjpegResponses.size());
      try {
        response.close();
      } catch (IOException ignore) {
      }
    }
  }

  private void serveMotionJpegFrame(byte[] jpegData, OutputStream outputStream) throws IOException {
    outputStream.write("--ipcamera\r\n".getBytes());
    outputStream.write("Content-Type: image/jpeg\r\n".getBytes());
    outputStream.write(("Content-Length: " + jpegData.length + "\r\n\r\n").getBytes());
    outputStream.write(jpegData);
    log.atFine().log("Wrote mJpeg frame");
  }

  /** Serves the given data and content type to the given response. */
  private void serveData(String contentType, byte[] data, Response response) throws IOException {
    response.setStatus(Status.OK);
    response.setContentType(contentType);
    response.getOutputStream().write(data);
    response.getOutputStream().close();
  }
}
