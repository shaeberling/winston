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
import com.s13g.winston.common.io.DataLoader;
import com.s13g.winston.common.io.ResourceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;

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
  private class ImageServerContainer implements Container {
    @Override
    public void handle(Request request, Response response) {
      String requestUrl = request.getAddress().toString();
      try {
        if ("/".equals(requestUrl)) {
          serveData("text/html", mIndexPageBytes, response);
        } else if (requestUrl.startsWith(CURRENT_IMAGE_PATH)) {
          serveCurrentFile(response);
        } else {
          response.setStatus(Status.NOT_FOUND);
        }
      } catch (IOException e) {
        LOG.error("Error while serving [" + requestUrl + "]: " + e.getMessage());
      } finally {
        try {
          response.close();
        } catch (IOException e) {
          LOG.warn("Cannot close response: " + e.getMessage());
        }
      }
    }
  }

  private static final Logger LOG = LogManager.getLogger(ImageServer.class);
  private static final int NUM_SERVER_THREADS = 10;
  private static final String HTML_TEMPLATE_FILE = "/sauron.html";
  private static final String CURRENT_IMAGE_PATH = "/now.jpg";

  /** Loads resources, such as the index file via the classpath. */
  private final ResourceLoader mResourceLoader;
  /** Used to serve containers. */
  private final ContainerServer mContainerServer;
  /** Used to read the file into memory to serve it. */
  private final Executor mFileReadExecutor;
  /** The bytes of the current image to serve. */
  @GuardedBy("mBytesLock")
  private byte[] mCurrentImageBytes;
  /** Locks access on the mCurrentImageBytes object */
  private final Object mBytesLock;
  /** The main HTML site. */
  private byte[] mIndexPageBytes;

  /**
   * Creates a new image server.
   *
   * @param port the port to serve requests on.
   * @param fileReadExecutor the executor to use to read new image files into memory.
   */
  public ImageServer(int port, ContainerServer.Creator serverCreator,
                     Executor fileReadExecutor, ResourceLoader resourceLoader) {
    mContainerServer = serverCreator.create(port, NUM_SERVER_THREADS);
    mFileReadExecutor = fileReadExecutor;
    mResourceLoader = resourceLoader;
    mCurrentImageBytes = new byte[0];
    mBytesLock = new Object();
    mIndexPageBytes = new byte[0];
  }

  /**
   * Starts a webserver to serve the webcam images.
   */
  public void start() {
    ImageServerContainer container = new ImageServerContainer();
    try {
      loadIndexPage();
    } catch (IOException e) {
      LOG.error("Cannot start webserver: " + e.getMessage());
      return;
    }
    mContainerServer.startServing(container);
  }

  /**
   * Set the current image file. This will make the file at the given location being loaded into
   * memory from where it will be served when requested. Setting a new current file will override
   * the previous one.
   *
   * @param currentImageLoader loads the current image file.
   */
  public void setCurrentFile(DataLoader currentImageLoader) {
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
  private void serveCurrentFile(Response response) throws IOException {
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

  /** Load the index page so it can be easily served from memory. */
  private void loadIndexPage() throws IOException {
    mIndexPageBytes = mResourceLoader.load(HTML_TEMPLATE_FILE);
  }
}
