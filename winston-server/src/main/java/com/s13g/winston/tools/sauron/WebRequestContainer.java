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

package com.s13g.winston.tools.sauron;

import com.google.common.flogger.FluentLogger;
import com.s13g.winston.common.ContainerServer;
import com.s13g.winston.common.io.ResourceLoader;



import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;

import java.io.IOException;

/**
 * Contaisner for the web frontend of Sauron. Handles all incoming requests.
 */
class WebRequestContainer implements Container {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private static final int NUM_SERVER_THREADS = 10;
  private static final String CURRENT_IMAGE_PATH = "/now.jpg";
  private static final String HTML_TEMPLATE_FILE = "/sauron.html";
  private static final String FOSCAM_VIDEO_STREAM = "/videostream.cgi";

  private final ImageServer mImageServer;

  /** Loads resources, such as the index file via the classpath. */
  private final ResourceLoader mResourceLoader;
  /** Used to serve containers. */
  private final ContainerServer mContainerServer;


  /** The main HTML site. */
  private byte[] mIndexPageBytes;

  WebRequestContainer(int port, ContainerServer.Creator serverCreator,
                      ImageServer imageServer, ResourceLoader resourceLoader) {
    mImageServer = imageServer;
    mResourceLoader = resourceLoader;
    mContainerServer = serverCreator.create(port, NUM_SERVER_THREADS);
    mIndexPageBytes = new byte[0];
  }

  /**
   * Starts a webserver to serve the webcam images.
   */
  void start() {
    try {
      loadIndexPage();
    } catch (IOException e) {
      log.atSevere().log("Cannot start webserver: " + e.getMessage());
      return;
    }
    mContainerServer.startServing(this);
  }

  @Override
  public void handle(Request request, Response response) {
    String requestUrl = request.getAddress().toString();
    boolean closeResponse = true;
    try {
      if ("/".equals(requestUrl)) {
        serveData("text/html", mIndexPageBytes, response);
      } else if (requestUrl.startsWith(CURRENT_IMAGE_PATH)) {
        mImageServer.serveCurrentFile(response);
      } else if (requestUrl.startsWith(FOSCAM_VIDEO_STREAM)) {
        mImageServer.serveMotionJpegAsync(response);
        closeResponse = false;
      } else {
        response.setStatus(Status.NOT_FOUND);
      }
    } catch (IOException e) {
      log.atSevere().log("Error while serving [" + requestUrl + "]: " + e.getMessage());
    } finally {
      if (closeResponse) {
        try {
          response.close();
        } catch (IOException e) {
          log.atWarning().log("Cannot close response: " + e.getMessage());
        }
      }
    }
  }

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