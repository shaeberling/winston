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

package com.s13g.winston.lib.temperature;

import com.s13g.winston.lib.core.file.ReadableFile;
import com.s13g.winston.lib.plugin.NodePluginType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DS18B20ControllerImpl}
 */
public class DS18B20ControllerImplTest {

  ReadableFile.Creator mFileCreator;
  private static final String VALID_INPUT_1 =
      "4f 01 4b 46 7f ff 01 10 cf : crc=cf YES\n" +
          "4f 01 4b 46 7f ff 01 10 cf t=20912";
  private static final String VALID_INPUT_2 =
      "4f 01 4b 46 7f ff 01 10 cf : crc=cf YES\n" +
          "4f 01 4b 46 7f ff 01 10 cf t=42123";
  private static final String VALID_INPUT_INVALID_READ =
      "4f 01 4b 46 7f ff 01 10 cf : crc=cf NO\n" +
          "4f 01 4b 46 7f ff 01 10 cf t=20912";
  private static final String INVALID_INPUT_1 =
      "4f 01 4b 46 7f ff 01 10 cf : crc=cf\n" +
          "4f 01 4b 46 7f ff 01 10 cf t=20912";
  private static final String INVALID_INPUT_2 =
      "4f 01 4b 46 7f ff 01 10 cf : crc=cf YES\n" +
          "4f 01 4b 46 7f ff 01 10 cf";
  private static final String INVALID_INPUT_3 =
      "This makes not sense\nat all.";
  private static final String INVALID_INPUT_NO_NUMBER =
      "4f 01 4b 46 7f ff 01 10 cf : crc=cf YES\n" +
          "4f 01 4b 46 7f ff 01 10 cf t=notANumber";

  @Before
  public void initialize() {
    mFileCreator = mock(ReadableFile.Creator.class);
  }

  @Test
  public void testIsCorrectType() {
    DS18B20ControllerImpl controller =
        new DS18B20ControllerImpl("SuperDuperDevice42", mFileCreator);
    assertEquals(NodePluginType.DS18B20_TEMP, controller.getType());
  }

  @Test
  public void testAccessCorrectFile() {
    DS18B20ControllerImpl controller =
        new DS18B20ControllerImpl("SuperDuperDevice42", mFileCreator);
    ArgumentCaptor<Path> pathArgument = ArgumentCaptor.forClass(Path.class);
    verify(mFileCreator).create(pathArgument.capture());
    assertEquals(Paths.get("/sys/bus/w1/devices/SuperDuperDevice42/w1_slave"), pathArgument
        .getValue());
  }

  @Test
  public void testDefaultTempWhenFileDoesNotExist() {
    ReadableFile fakeFile = new ReadableFile() {
      @Override
      public boolean exists() {
        return false;
      }

      @Override
      public boolean isReadable() {
        return true;
      }

      @Override
      public String readAsString() throws IOException {
        return VALID_INPUT_1;
      }
    };
    when(mFileCreator.create(any(Path.class))).thenReturn(fakeFile);
    DS18B20ControllerImpl controller =
        new DS18B20ControllerImpl("SuperDuperDevice42", mFileCreator);
    TemperatureSensorController.Temperature temperature = controller.getTemperature();
    assertEquals("-42.0 C", temperature.toString());
  }

  @Test
  public void testDefaultTempWhenFileIsNotReadable() {
    ReadableFile fakeFile = new ReadableFile() {
      @Override
      public boolean exists() {
        return true;
      }

      @Override
      public boolean isReadable() {
        return false;
      }

      @Override
      public String readAsString() throws IOException {
        return VALID_INPUT_1;
      }
    };
    when(mFileCreator.create(any(Path.class))).thenReturn(fakeFile);
    DS18B20ControllerImpl controller =
        new DS18B20ControllerImpl("SuperDuperDevice42", mFileCreator);
    TemperatureSensorController.Temperature temperature = controller.getTemperature();
    assertEquals("-42.0 C", temperature.toString());
  }

  @Test
  public void testValidInput() {
    ReadableFile fakeFile = createFileWithOutput(VALID_INPUT_1);
    when(mFileCreator.create(any(Path.class))).thenReturn(fakeFile);
    DS18B20ControllerImpl controller =
        new DS18B20ControllerImpl("SuperDuperDevice42", mFileCreator);
    TemperatureSensorController.Temperature temperature = controller.getTemperature();
    assertEquals("20.912 C", temperature.toString());
  }

  @Test
  public void testValidInputButInvalidRead() {
    ReadableFile fakeFile = createFileWithOutput(VALID_INPUT_INVALID_READ);
    when(mFileCreator.create(any(Path.class))).thenReturn(fakeFile);
    DS18B20ControllerImpl controller =
        new DS18B20ControllerImpl("SuperDuperDevice42", mFileCreator);
    TemperatureSensorController.Temperature temperature = controller.getTemperature();
    assertEquals("-42.0 C", temperature.toString());
  }

  @Test
  public void testInvalidInput1() {
    ReadableFile fakeFile = createFileWithOutput(INVALID_INPUT_1);
    when(mFileCreator.create(any(Path.class))).thenReturn(fakeFile);
    DS18B20ControllerImpl controller =
        new DS18B20ControllerImpl("SuperDuperDevice42", mFileCreator);
    TemperatureSensorController.Temperature temperature = controller.getTemperature();
    assertEquals("-42.0 C", temperature.toString());
  }

  @Test
  public void testInvalidInput2() {
    ReadableFile fakeFile = createFileWithOutput(INVALID_INPUT_2);
    when(mFileCreator.create(any(Path.class))).thenReturn(fakeFile);
    DS18B20ControllerImpl controller =
        new DS18B20ControllerImpl("SuperDuperDevice42", mFileCreator);
    TemperatureSensorController.Temperature temperature = controller.getTemperature();
    assertEquals("-42.0 C", temperature.toString());
  }

  @Test
  public void testInvalidInput3() {
    ReadableFile fakeFile = createFileWithOutput(INVALID_INPUT_3);
    when(mFileCreator.create(any(Path.class))).thenReturn(fakeFile);
    DS18B20ControllerImpl controller =
        new DS18B20ControllerImpl("SuperDuperDevice42", mFileCreator);
    TemperatureSensorController.Temperature temperature = controller.getTemperature();
    assertEquals("-42.0 C", temperature.toString());
  }

  @Test
  public void testInvalidInputNoNumber() {
    ReadableFile fakeFile = createFileWithOutput(INVALID_INPUT_NO_NUMBER);
    when(mFileCreator.create(any(Path.class))).thenReturn(fakeFile);
    DS18B20ControllerImpl controller =
        new DS18B20ControllerImpl("SuperDuperDevice42", mFileCreator);
    TemperatureSensorController.Temperature temperature = controller.getTemperature();
    assertEquals("-42.0 C", temperature.toString());
  }

  @Test
  public void testValidInputUpdatedSecondTime() {
    ReadableFile fakeFile = createFileWithTwoOutputs(VALID_INPUT_1, VALID_INPUT_2);
    when(mFileCreator.create(any(Path.class))).thenReturn(fakeFile);
    DS18B20ControllerImpl controller =
        new DS18B20ControllerImpl("SuperDuperDevice42", mFileCreator);
    assertEquals("20.912 C", controller.getTemperature().toString());
    assertEquals("42.123 C", controller.getTemperature().toString());
  }

  @Test
  public void testValidInputAfterInvalidInput() {
    ReadableFile fakeFile = createFileWithTwoOutputs(INVALID_INPUT_1, VALID_INPUT_2);
    when(mFileCreator.create(any(Path.class))).thenReturn(fakeFile);
    DS18B20ControllerImpl controller =
        new DS18B20ControllerImpl("SuperDuperDevice42", mFileCreator);
    assertEquals("-42.0 C", controller.getTemperature().toString());
    assertEquals("42.123 C", controller.getTemperature().toString());
  }

  @Test
  public void testInvalidInputAfterValidNoChange() {
    ReadableFile fakeFile = createFileWithTwoOutputs(VALID_INPUT_2, INVALID_INPUT_1);
    when(mFileCreator.create(any(Path.class))).thenReturn(fakeFile);
    DS18B20ControllerImpl controller =
        new DS18B20ControllerImpl("SuperDuperDevice42", mFileCreator);
    assertEquals("42.123 C", controller.getTemperature().toString());
    // An invalid input should not alter the last known good temperature reading.
    assertEquals("42.123 C", controller.getTemperature().toString());
  }

  @Test
  public void testReadingFileThrowsException() {
    ReadableFile fakeFile = new ReadableFile() {
      @Override
      public boolean exists() {
        return true;
      }

      @Override
      public boolean isReadable() {
        return true;
      }

      @Override
      public String readAsString() throws IOException {
        throw new IOException("Just a test");
      }
    };
    when(mFileCreator.create(any(Path.class))).thenReturn(fakeFile);
    DS18B20ControllerImpl controller =
        new DS18B20ControllerImpl("SuperDuperDevice42", mFileCreator);
    assertEquals("-42.0 C", controller.getTemperature().toString());
  }

  private static ReadableFile createFileWithOutput(final String output) {
    return new ReadableFile() {
      @Override
      public boolean exists() {
        return true;
      }

      @Override
      public boolean isReadable() {
        return true;
      }

      @Override
      public String readAsString() throws IOException {
        return output;
      }
    };
  }

  /** Different output the first from the second time it is being called. */
  private static ReadableFile createFileWithTwoOutputs(final String output1, final String output2) {
    return new ReadableFile() {
      private boolean mFirst = true;

      @Override
      public boolean exists() {
        return true;
      }

      @Override
      public boolean isReadable() {
        return true;
      }

      @Override
      public String readAsString() throws IOException {
        if (mFirst) {
          mFirst = false;
          return output1;
        } else {
          return output2;
        }
      }
    };
  }
}
