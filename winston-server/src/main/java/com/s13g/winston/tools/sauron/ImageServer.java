/*
 * Copyright 2015 Sascha Haeberling
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

import com.google.common.io.ByteStreams;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;

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

  /** The port to serve the HTTP requests on. */
  private final int mPort;
  /** Used to read the file into memory to serve it. */
  private final ExecutorService mFileReadExecutor;
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
   * @param port             the port to serve requests on.
   * @param fileReadExecutor the executor to use to read new image files into memory.
   */
  public ImageServer(int port, ExecutorService fileReadExecutor) {
    mPort = port;
    mFileReadExecutor = fileReadExecutor;
    mBytesLock = new Object();
  }

  /**
   * Starts a webserver to serve the webcam images.
   */
  public void start() {
    ImageServerContainer container = new ImageServerContainer();
    try {
      loadIndexPage();
      ContainerSocketProcessor processor =
          new ContainerSocketProcessor(container, NUM_SERVER_THREADS);
      Connection connection = new SocketConnection(processor);
      SocketAddress address = new InetSocketAddress(mPort);
      connection.connect(address);
      LOG.info("Listening to: " + address.toString());
    } catch (IOException e) {
      LOG.error("Cannot start webserver: " + e.getMessage());
    }
  }

  /**
   * Set the current image file. This will make the file at the given location being loaded into
   * memory from where it will be served when requested. Setting a new current file will override
   * the previous one.
   *
   * @param currentImage the current image file.
   */
  public void setCurrentFile(final File currentImage) {
    mFileReadExecutor.execute(() -> {
      FileInputStream fileInputStream;
      try {
        fileInputStream = new FileInputStream(currentImage);
      } catch (FileNotFoundException e) {
        LOG.error("Cannot not find current image file [" + currentImage.getAbsolutePath() + "]: "
            + e.getMessage());
        return;
      }
      byte[] newImageBytes;
      try {
        newImageBytes = ByteStreams.toByteArray(fileInputStream);
      } catch (IOException e) {
        LOG.error("Cannot read file [" + currentImage.getAbsolutePath() + "]: " + e.getMessage());
        return;
      }
      synchronized (mBytesLock) {
        mCurrentImageBytes = newImageBytes;
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
    response.setContentType(contentType);
    response.getOutputStream().write(data);
    response.getOutputStream().close();
  }

  /** Load the index page so it can be easily served from memory. */
  private void loadIndexPage() throws IOException {
    InputStream indexPageStream = getClass().getResourceAsStream(HTML_TEMPLATE_FILE);
    if (indexPageStream == null) {
      throw new IOException("Cannot load index page [" + HTML_TEMPLATE_FILE + "]");
    }
    mIndexPageBytes = ByteStreams.toByteArray(indexPageStream);
  }
}
