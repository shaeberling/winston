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

package com.s13g.winston.lib.core.util.file;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Default implementation for file functionality.
 */
@ParametersAreNonnullByDefault
public class FileWrapperImpl implements FileWrapper {
  private final Path mPath;

  public FileWrapperImpl(Path path) {
    mPath = Preconditions.checkNotNull(path);
  }

  @Override
  public boolean isRegularFile() {
    return Files.isRegularFile(mPath);
  }

  @Override
  public boolean deleteIfExists() throws IOException {
    return Files.deleteIfExists(mPath);
  }

  @Override
  public BasicFileAttributes readBasicAttributes() throws IOException {
    return Files.readAttributes(mPath, BasicFileAttributes.class);
  }

  @Override
  public String toString() {
    return mPath.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FileWrapperImpl)) {
      return false;
    }
    return mPath.equals(((FileWrapperImpl) obj).mPath);
  }
}
