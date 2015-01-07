winston
=======

This is the Winston Home Automation system.

There are several parts to the whole system and this readme fill be extended
in the future as the different parts are getting fleshed out:

 * **winston-android** is an Android client for communicating with the Winston servers.
 * **winston-libs** is a set of libraries used by servers to e.g. interface with GPIO
 * **winston-server** contains the master and node servers. The latter is run on the Winston nodes, communicates to sensors and actuators directly, and the former is responsible for collecting data from the nodes and communicates with the clients.

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

[![Build Status](https://travis-ci.org/shaeberling/winston.svg)](https://travis-ci.org/shaeberling/winston)
