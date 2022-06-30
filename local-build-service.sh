#!/usr/bin/env bash

mvn -Dmaven.repo.local=$(pwd)/.m2/repository -DskipTests clean package

NAME=$1
MODULE="esd-$NAME"

if [ "$NAME" = "server" ]
then
  PORT=58001
  MONITOR=60001
elif [ "$NAME" = "client" ]
then
  PORT=8012
  MONITOR=60002
elif [ "$NAME" = "auth" ]
then
  PORT=57000
  MONITOR=60010
else
  PORT=0
  MONITOR=0
fi

docker build -t esd-integrate/esd-$NAME \
      -f Dockerfile.local \
      --build-arg NAME=$NAME \
      --build-arg MODULE=$MODULE \
      --build-arg PORT=$PORT \
      --build-arg MONITOR=$MONITOR \
      .

echo 'remove <none> images'
docker rmi `docker images | grep "<none>" | awk {'print $3'}`

