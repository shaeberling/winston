#!/bin/bash

# Make all available as non-nano Java classes.
protoc --java_out=src/main/java etc/protos/*.proto

# The ones we need to access on Android we convert to nano.
protoc --javanano_out=../winston-android/src/main/java etc/protos/for_clients.proto

