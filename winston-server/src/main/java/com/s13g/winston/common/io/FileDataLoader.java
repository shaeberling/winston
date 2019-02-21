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

package com.s13g.winston.common.io;

import com.google.common.flogger.FluentLogger;
import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import javax.annotation.concurrent.GuardedBy;

/**
 * Loads data from a given file. Doesn't cache loaded data.
 */
public class FileDataLoader implements DataLoader {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  @GuardedBy("mLock")
  private final File mFile;
  private final Object mLock;

  public FileDataLoader(File file) {
    mFile = file;
    mLock = new Object();
  }

  @Override
  public Optional<byte[]> load() {
    synchronized (mLock) {
      FileInputStream fileInputStream;
      try {
        fileInputStream = new FileInputStream(mFile);
      } catch (FileNotFoundException e) {
        log.atWarning().log("Cannot not find file [%s]: %s",
            mFile.getAbsolutePath(), e.getMessage());
        return Optional.empty();
      }
      try {
        return Optional.of(ByteStreams.toByteArray(fileInputStream));
      } catch (IOException e) {
        log.atWarning().log("Cannot read file [%s]: %s",
            mFile.getAbsolutePath(), e.getMessage());
        return Optional.empty();
      }
    }
  }
}
