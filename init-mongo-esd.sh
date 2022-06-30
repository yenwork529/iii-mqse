#!/bin/bash

WORKDIR=$(pwd)
MOUNTDIR="/app"

docker run -it --rm -v $WORKDIR:$MOUNTDIR mongo mongo --host "mongodb://iii:dtiisno1!@140.92.24.20:39999/ESD?authSource=admin" "$MOUNTDIR/esd-deploy/docker/server/create-esd-documents.js"

