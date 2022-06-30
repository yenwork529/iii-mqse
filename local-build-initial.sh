#!/usr/bin/env bash

mvn -Dmaven.repo.local=$(pwd)/.m2/repository -DskipTests clean package

docker build -t esd-integrate/esd-initial \
      -f Dockerfile.localInitial \
      .
