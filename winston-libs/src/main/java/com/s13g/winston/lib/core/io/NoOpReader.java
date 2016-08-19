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

package com.s13g.winston.lib.core.io;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * A reader that does nothing.
 */
public class NoOpReader extends Reader {

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    return 0;
  }

  @Override
  public void close() throws IOException {
  }
}
