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

package com.s13g.winston.lib.core.file;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Readable file interface.
 */
public interface ReadableFile {
  /** Whether the file exits. */
  boolean exists();

  /** Whether the file is readable. */
  boolean isReadable();

  /**
   * Reads the whole file as a string.
   *
   * @return The file contents.
   * @throws IOException if reading the file contents failed.
   */
  String readAsString() throws IOException;


  /** Creates readable files. */
  public static class Creator {
    /** Create a file handle with the given path. */
    public ReadableFile create(Path path) {
      return new ReadableFileImpl(path);
    }
  }
}
