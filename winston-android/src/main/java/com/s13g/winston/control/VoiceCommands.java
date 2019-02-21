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

package com.s13g.winston.control;

import android.content.Context;

import com.google.common.flogger.FluentLogger;
import com.s13g.winston.control.filter.IOperationFilter;
import com.s13g.winston.control.filter.StringComparisonFilter;
import com.s13g.winston.control.filter.compare.EditDistancePlugin;
import com.s13g.winston.control.operation.IOperationProcess;
import com.s13g.winston.control.operation.OperationBroker;
import com.s13g.winston.control.type.IOperationFactory;
import com.s13g.winston.control.type.OperationFactoryImpl;

import java.util.List;

/**
 * Handles voice commands.
 * <p>
 * Needs to be refactored. Currently not working.
 */
public class VoiceCommands {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  // control
  private static final int TOLERANCE = 1;
  // TODO: currently, commands are only for test purposes
  private static final String[] KNOWN_WORDS = {"light on", "light off", "open garage", "close " +
      "garage"};

  private final IOperationFilter mOperationFilter;
  private final OperationBroker mCommandBroker;

  public static VoiceCommands create(Context context) {
    StringComparisonFilter operationFilter = new StringComparisonFilter(
        new EditDistancePlugin(), KNOWN_WORDS, TOLERANCE);

    OperationBroker commandBroker = new OperationBroker.Builder(context)
        .setOperationFactory(new OperationFactoryImpl())
        .setOperationProcessor(new IOperationProcess() {
          @Override
          public void processRecognizedOperationResults(List<String> recognizedOperations,
                                                        float[] confidenceScore) {
//            recognizedOperations = filter(recognizedOperations);
//
//            if (recognizedOperations.size() == 1) {
//              final String operation = recognizedOperations.get(0);
//              // TODO: operation only for test purposes and also doubled (see above)
//              if (operation.equals("light on")) {
//                // TODO
//              } else if (operation.equals("light off")) {
//                // TODO
//              } else if (operation.equals("open garage")) {
//                // TODO
//              } else if (operation.equals("close garage")) {
//                // TODO
//              } else {
//                // TODO
//              }
//            } else {
//              log.atInfo().log("no command processing");
//            }
          }
        })
        .setOperationType(IOperationFactory.OperationType.SPEECH_MANUAL)
        .build();
    return new VoiceCommands(operationFilter, commandBroker);
  }

  VoiceCommands(IOperationFilter operationFilter, OperationBroker commandBroker) {
    mOperationFilter = operationFilter;
    mCommandBroker = commandBroker;
  }

  public void onStart() {
    mCommandBroker.onStart();
  }

  public List<String> filter(List<String> recognizedOperations) {
    return mOperationFilter.filter(recognizedOperations);
  }

//  @Override
//  public void processRecognizedOperationResults(List<String> recognizedOperations, float[]
//      confidenceScore) {
//    recognizedOperations = mVoiceCommands.filter(recognizedOperations);
//
//    if (recognizedOperations.size() == 1) {
//      final String operation = recognizedOperations.get(0);
//      // TODO: operation only for test purposes and also doubled (see above)
//      if (operation.equals("light on")) {
//        onActionLightOn();
//      } else if (operation.equals("light off")) {
//        onActionLightOff();
//      } else if (operation.equals("open garage")) {
//        onActionOpenGarage();
//      } else if (operation.equals("close garage")) {
//        onActionCloseGarage();
//      } else {
//        log.atWarning().log("unkown operation");
//      }
//    } else {
//      log.atInfo().log("no command processing");
//    }
//  }
}
