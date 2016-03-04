#!/bin/sh

./gradlew test --stacktrace && \
./gradlew checkNullLibs && \
./gradlew checkLocksLibs && \
./gradlew checkNullServer && \
./gradlew checkLocksServer

