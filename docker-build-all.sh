#!/bin/bash

BUILD=$1

echo 'Build the base image.'
docker build -t esd-java/esd-build -f ./Dockerfile.build .

echo 'Build JDK image.'
docker build -t esd-java/esd-jdk -f ./Dockerfile.jdk .

NAMESPACE=esd-integrate
MODULES=("client" "auth" "server" "initial" "tester")

for(( i = 0; i < ${#MODULES[@]}; i++))
do
        MODULE=${MODULES[i]}

	echo "Build the $MODULE image."
	docker build -t $NAMESPACE/esd-$MODULE -f ./Dockerfile.$MODULE .
done

echo 'Remove <none> images.'
docker rmi `docker images | grep "<none>" | awk {'print $3'}`
