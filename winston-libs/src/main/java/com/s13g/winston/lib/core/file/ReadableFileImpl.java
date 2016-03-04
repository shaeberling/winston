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

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Simple file interface that can easily be mocked in tests.
 */
@ParametersAreNonnullByDefault
public class ReadableFileImpl implements ReadableFile {
  private final Path mPath;

  ReadableFileImpl(Path path) {
    Preconditions.checkNotNull(path);
    mPath = path;
  }

  @Override
  public boolean exists() {
    return Files.exists(mPath);
  }

  @Override
  public boolean isReadable() {
    return Files.isReadable(mPath);
  }

  @Override
  public String readAsString() throws IOException {
    return new String(Files.readAllBytes(mPath));
  }

  @Override
  public String toString() {
    return mPath.toString();
  }
}
