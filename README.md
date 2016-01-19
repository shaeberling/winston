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
   * **sauron webcam** Sauron is a stand-alone daemon that connects to a webcam and shoots pictures at a set interval. Pictures are stored on disk in a configurable path. Sauron also has storage space protection built in: Once the free space within the image repository path falls below a configurable threshold, Sauron will start to remove the oldest files. Sauron's main use case is as a security camera. The newest image taken will be served from within the build-in webserver.

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
To build Winston you need a Java 8 JDK since the code makes use of the latest language features 
such as lambdas and streams. 
To build the different components, use the following commands from the 
project root directory:


Target | Build command
--- | --- 
Node Daemon |  `./gradlew nodeDaemon` 
Master Daemon |  `./gradlew masterDaemon` 
Sauron Daemon |  `./gradlew sauronDaemon` 
Android App |  `./gradlew winston-android:assembleDebug` 
Android Wear App |  `./gradlew winston-android-wear:assembleDebug` 
Run all tests | `./gradlew test`

### Installation
Once you have compiled the daemons, simply copy them to your server or node. You can find them in
 the following locations:
 
- `winston-server/build/libs/winston-node-daemon.jar` 
- `winston-server/build/libs/winston-master-daemon.jar` 
- - `winston-server/build/libs/winston-sauron-daemon.jar` 


### Configuration and Running
For the **master**, have a file `master.config` in the path then launch via `java -jar 
winston-master-daemon.jar`.

A master.config could look like this (subject to change):
```
daemon_port:1984
node_mapping: {
  name: "node-1"
  address: 192.168.12.34
  port: 1984
  use_ssl: false
}
node_mapping: {
  name: "node-2"
  address: 192.168.12.35
  port: 1984
  use_ssl: false
}
```

For the **nodes**, have a file `node.config` in the path and then launch via `java -jar 
winston-node-daemon.jar`.

A node.config could look like this (subject to change):
```
daemon_port: 1984
active_plugins: {
  name: "relay"
  mapping: 1
  mapping: 3
  mapping: 2
  mapping: 4
}
active_plugins: {
  name: "reed"
  mapping: 5
  mapping: 6
}
```

