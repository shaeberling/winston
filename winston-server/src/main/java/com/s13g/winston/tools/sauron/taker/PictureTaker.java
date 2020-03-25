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

package com.s13g.winston.tools.sauron.taker;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;

import javax.annotation.Nonnull;

/** Common interface for picture taker. */
public interface PictureTaker {

  enum Command {
    FSWEBCAM("/usr/bin/fswebcam -r 1920x1080 %s"),
    RASPISTILL("raspistill -o %s"),
    RASPISTILL_FLIP("raspistill -vf -hf -o %s");

    final String commandLine;

    Command(String cmd) {
      this.commandLine = cmd;
    }
  }

  /**
   * Captures an image and writes it to the give file.
   *
   * @param file the path to which to write the final image file to.
   */
  @Nonnull
  ListenableFuture<Boolean> captureImage(File file);
}
