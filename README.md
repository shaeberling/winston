winston
=======

This is the Winston Home Automation system.

There are several parts to the whole system and I will extens this readme
in the future as the different parts are getting fleshed out:

 * **winston-android** is an Android client for communicating with the Winston server.
 * **winston-node** is the service that is deployed to winston nodes
 * **winston-node-libs** is a set of libraries used by nodes to e.g. interface with GPIO
 * **winston-server** is the master server that is responsible for collecting data from the nodes

The goal is to run the nodes on Raspberry Pi devices and to connect various
sensors and actuators to these nodes. The nodes then communicate to the
server which collects the data and provides a secure and encrypted way
for the nodes and other clients to communicate.

Clients, such as web APIs or Android clients then communicate only through the
server, which should also be able to run on a Raspberry Pi, but can run on any
hardware that supports the JVM.

Speaking of which, the goal of this project is to use Java throughout. But we
might make use of existing libraries and wrap them in a way they can be called
from Winston.
