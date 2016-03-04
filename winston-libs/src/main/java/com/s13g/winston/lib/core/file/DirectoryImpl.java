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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Default implementation for the Directory interface.
 */
@ParametersAreNonnullByDefault
public class DirectoryImpl implements Directory {
  private final Path mPath;

  public static Directory create(String path) {
    return create(Paths.get(path));
  }

  public static Directory create(Path path) {
    Preconditions.checkNotNull(path);
    if (!Files.isDirectory(path)) {
      throw new IllegalArgumentException("The given path is not a directory: " + path);
    }
    return new DirectoryImpl(path);
  }

  DirectoryImpl(Path path) {
    mPath = path;
  }

  @Override
  public void walkFileTree(final SimpleFileVisitor visitor) throws IOException {
    Files.walkFileTree(mPath, new java.nio.file.SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        visitor.visitFile(new FileWrapperImpl(file));
        return FileVisitResult.CONTINUE;
      }
    });
  }

  @Override
  public File getFile() {
    return mPath.toFile();
  }

  @Override
  public Path getPath() {
    return mPath;
  }
}
