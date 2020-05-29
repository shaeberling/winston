# Winston
This is the Winston Home Automation system.

[![Build Status](https://travis-ci.org/shaeberling/winston.svg)](https://travis-ci.org/shaeberling/winston)
[![Coverage Status](https://coveralls.io/repos/shaeberling/winston/badge.svg?branch=master)](https://coveralls.io/r/shaeberling/winston?branch=master)

## Project Structure
There are several parts to the whole system and this readme fill be extended
in the future as the different parts are getting fleshed out:

 * **winston-android** is an Android client for communicating with the Winston servers.
 * **winston-android-wear** is an Android Wear client that runs on a watch and communicated with 
 the winston-android app.
 * **winston-libs** is a set of libraries used by servers to e.g. interface with GPIO
 * **winston-server** contains the master and node servers. The latter is run on the Winston nodes,
 communicates to sensors and actuators directly, and the former is responsible for collecting data
 from the nodes and communicates with the clients.

## Goal
The goal is to run the nodes on Raspberry Pi devices and to connect various
sensors and actuators to these nodes. The nodes then communicate to the
server which collects the data and provides a secure and encrypted way
for the nodes and other clients to communicate.

Clients, such as web APIs or Android clients then communicate only through the
server, which should also be able to run on a Raspberry Pi, but can run on any
hardware that supports the JVM.

Speaking of which, the goal of this project is to use Java where possible. But we
might make use of existing libraries and wrap them in a way they can be called
from Winston.

## Building, Installing, Configuration and Running
### Build
To build Winston you need a Java 8 JDK since the code makes use of language features 
such as lambdas and streams. 
To build the different components, use the following commands from the 
project root directory:


Target | Build command
--- | --- 
Node Daemon |  `./gradlew nodeDaemon` 
Master Daemon |  `./gradlew masterDaemon` 
Android App |  `./gradlew winston-android:assembleDebug` 
Android Wear App |  `./gradlew winston-android-wear:assembleDebug`

### Installation
Once you have compiled the daemons, simply copy them to your server or node. You can find them in
 the following locations:
 
- `winston-server/build/libs/winston-{version}-master.jar`
- `winston-server/build/libs/winston-{version}-node.jar`

Dependencies:

sudo apt-get install wiringpi
also: java


### Configuration and Running
For the **master**, have a file `master.config` in the path then launch via `java -jar 
winston-master-daemon.jar`.

A master.config could look like this (subject to change):
```
daemon_port:1981
auth_client {
  name: "John Doe"
  auth_token: "{secret token} 
}
module {
  type: "winston"
  channel {
    type: "sensors"
    address: "raspberrypi"
    parameter {
      name: "temp-sensor"
      value: "htu21d_temp_humid/"
    }
  }
}
```

For the **nodes**, have a file `node.config` in the path and then launch via `sudo java -jar 
winston-node-daemon.jar`. It is necessary to launch the node daemon with `sudo` so that it can access the GPIO pins.

A node.config could look like this (subject to change):
```
daemon_port: 1984
i2c_plugin {
  type: "HTU21D_TEMP_HUMID"
  bus: 1
  address: 0x40
}
gpio_plugins: {
  name: "relay"
  mapping: 1
  mapping: 3
  mapping: 2
  mapping: 4
}
gpio_plugins: {
  name: "reed"
  mapping: 5
  mapping: 6
}
```
