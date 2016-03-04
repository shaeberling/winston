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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Path;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Easy to mock interface for dealing with directories.
 */
@ParametersAreNonnullByDefault
public interface Directory {
  /**
   * See {@link java.nio.file.Files#walkFileTree(java.nio.file.Path, java.nio.file.FileVisitor)}.
   */
  void walkFileTree(SimpleFileVisitor visitor) throws IOException;

  /** Returns the File representation of this directory. */
  File getFile();

  /** Returns the Path representation of this directory. */
  Path getPath();
}
