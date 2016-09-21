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

package com.s13g.winston.common;

import com.google.common.collect.Lists;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;


/**
 * Tests for {@link RefreshableData}.
 */
public class RefreshableDataTest {

  @Test
  public void testEmptyCollection() {
    RefreshableData<String> data = new RefreshableData<>(ArrayList::new, 10);
    assertEquals(0, data.size());
    assertEquals(true, data.isEmpty());
    try {
      data.get(0);
      fail("Should throw exception.");
    } catch (IndexOutOfBoundsException ex) {
      // Expected.
    }
  }

  @Test
  public void testBasics() {
    RefreshableData<String> data = new RefreshableData<>(RefreshableDataTest::getTestData, 10);
    assertEquals(3, data.size());
    assertFalse(data.isEmpty());
    assertEquals("Hello", data.get(0));
    assertEquals("dear", data.get(1));
    assertEquals("world", data.get(2));
  }

  @Test
  public void testRefresh() {
    // 0 TTL means there is going to be a refresh on every call.
    RefreshableData<String> data = new RefreshableData<>(new ProduceMoreAndMoreData(), 0);

    assertEquals(1, data.size());
    assertEquals(2, data.size());
    assertEquals(3, data.size());

    assertEquals("Hello", data.get(0));
    assertEquals("dear", data.get(1));
    assertEquals("world", data.get(2));
  }

  @Test
  public void testTimedRefresh() throws InterruptedException {
    // 0 TTL means there is going to be a refresh on every call.
    RefreshableData<String> data = new RefreshableData<>(new ProduceMoreAndMoreData(), 1);

    assertEquals(1, data.size());
    assertEquals(1, data.size());
    assertEquals(1, data.size());
    assertFalse(data.isEmpty());
    assertEquals("Hello", data.get(0));

    Thread.sleep(1000);
    assertEquals(2, data.size());
    assertEquals(2, data.size());
    assertEquals(2, data.size());
    assertFalse(data.isEmpty());
    assertEquals("dear", data.get(1));

    Thread.sleep(1000);
    assertEquals(3, data.size());
    assertEquals(3, data.size());
    assertEquals(3, data.size());
    assertFalse(data.isEmpty());
    assertEquals("world", data.get(2));
  }

  private static List<String> getTestData() {
    return Lists.newArrayList("Hello", "dear", "world");
  }

  private static class ProduceMoreAndMoreData implements Supplier<List<String>> {
    private final List<String> mSource = Lists.newArrayList("Hello", "dear", "world");
    private final List<String> mData;

    ProduceMoreAndMoreData() {
      mData = new ArrayList<>();
    }

    @Override
    public List<String> get() {
      if (mSource.isEmpty()) {
        return mData;
      }
      mData.add(mSource.remove(0));
      return mData;
    }
  }

}
