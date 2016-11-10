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

package com.s13g.winston.lib.clitests;

import com.s13g.winston.shared.data.Temperature;
import com.s13g.winston.lib.winston.WinstonController;
import com.s13g.winston.lib.winston.WinstonGarageNodeController;
import com.s13g.winston.lib.winston.WinstonSensorNodeController;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * CLI to test Winston node communication functionality.
 */
public class WinstonCli {
  public static void main(String[] args) {
    WinstonController controller = new WinstonController();
    WinstonSensorNodeController sensorNode = controller.getSensorNodeController("pi-cam-1");
    sensorNode.addTemperatureSensor("ds18b20_temp/0");

    for (Supplier<Optional<Temperature>> tempSupplier : sensorNode.getTemperatureSensors()) {
      Optional<Temperature> temperature = tempSupplier.get();
      if (!temperature.isPresent()) {
        System.err.println("Cannot read temperature");
      } else {
        System.out.println("Temperature: " + temperature.get().get(Temperature.Unit.CELSIUS));
      }
    }

    WinstonGarageNodeController garageController = controller.getGarageNodeController("pi-garage");
    garageController.addClicker("relay/0");
    garageController.addClicker("relay/1");
    garageController.addClosedState("reed/0");
    garageController.addClosedState("reed/1");

    for (Supplier<Optional<Boolean>> closedState : garageController.getClosedStates()) {
      Optional<Boolean> closedStateOpt = closedState.get();
      if (!closedStateOpt.isPresent()) {
        System.err.println("Cannot get closed state");
      }
      System.out.println("Garage is " + (closedStateOpt.get() ? "CLOSED" : "OPEN"));
    }

    Supplier<Boolean> clickerMainGarage = garageController.getClickers().get(0);
    boolean success = clickerMainGarage.get();
    System.out.println("Clicking garage was " + (success ? "SUCCESSFUL" : "UNSUCCESSFUL"));
  }
}
