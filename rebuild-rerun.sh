#!/bin/bash

echo 'shutdown docker containers'
docker-compose down

echo 'pull new source codes'
git pull origin new_dnp_api

echo 'build new images'
./docker-build.sh -b

echo 'start docker containers'
docker-compose up -d

