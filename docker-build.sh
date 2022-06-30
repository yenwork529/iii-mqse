#!/bin/bash

BUILD=$1

if [[ $BUILD -eq '-b' ]]
then
	echo 'Build the base image.'
	docker build -t esd-java/esd-build -f ./Dockerfile.build .

	echo 'Build JDK image.'
	docker build -t esd-java/esd-jdk -f ./Dockerfile.jdk .
fi

echo 'Build the initial image.'
docker build -t esd-integrate/esd-initial -f ./Dockerfile.init .

echo 'Build the server image.'
docker build -t esd-integrate/esd-server -f ./Dockerfile.server .

echo 'Build the auth image.'
docker build -t esd-integrate/esd-auth -f ./Dockerfile.auth .

echo 'Build the client image.'
docker build -t esd-integrate/esd-client -f ./Dockerfile.client .

if [[ $BUILD -eq '-b' ]]
then
	echo 'Remove <none> images.'
	docker rmi `docker images | grep "<none>" | awk {'print $3'}`
fi
