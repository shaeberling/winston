#!/bin/sh

./gradlew test && \
./gradlew checkNullLibs && \
./gradlew checkLocksLibs && \
./gradlew checkNullServer && \
./gradlew checkLocksServer

