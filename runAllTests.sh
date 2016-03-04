#!/bin/sh

./gradlew test -i && \
./gradlew checkNullLibs && \
./gradlew checkLocksLibs && \
./gradlew checkNullServer && \
./gradlew checkLocksServer

