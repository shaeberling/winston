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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reports whether enough free space is available on the given path.
 */
public class FreeSpaceReporterImpl implements FreeSpaceReporter {
  private static final Logger LOG = LogManager.getLogger(FreeSpaceReporterImpl.class);
  private final long mMinFreeBytes;
  private final FileStore mFileStore;

  /**
   * Creates a reporter for the given path.
   *
   * @param minFreeBytes the minimum number of free bytes required on the path. Once the available
   * space is less than this, {@link #isMinSpaceAvailable} will return false.
   */
  public static FreeSpaceReporterImpl from(long minFreeBytes, Path path) throws IOException {
    return new FreeSpaceReporterImpl(minFreeBytes, Files.getFileStore(path));
  }

  @Override
  public boolean isMinSpaceAvailable() {
    try {
      long freeSpaceBytes = mFileStore.getUsableSpace();
      LOG.info("Space available: " + bytesToMb(freeSpaceBytes) + " MB. (Max: " +
          bytesToMb(mMinFreeBytes) + "MB)");
      return freeSpaceBytes >= mMinFreeBytes;
    } catch (IOException ex) {
      LOG.error("Cannot determine free space.", ex);
      return false;
    }
  }

  private FreeSpaceReporterImpl(long minFreeBytes, FileStore fileStore) {
    mMinFreeBytes = minFreeBytes;
    mFileStore = fileStore;
  }

  private static long bytesToMb(long bytes) {
    return bytes / 1000000L;
  }
}
