#!/bin/bash

# REGISTRY=trial-web:54320
# harbor: docker tag SOURCE_IMAGE[:TAG] harbor-devops.iii.org.tw/c0rd27-esd-java/REPOSITORY[:TAG]
# REGISTRY=harbor-devops.iii.org.tw/c0rd27-esd-java
REGISTRY=trial-testing:54320
NAMESPACE=esd-integrate
MODULES=("esd-client" "esd-auth" "esd-server" "esd-initial" "esd-tester")

for(( i = 0; i < ${#MODULES[@]}; i++))
do
        MODULE=${MODULES[i]}

        docker rmi $REGISTRY/$NAMESPACE/$MODULE
        docker tag $NAMESPACE/$MODULE $REGISTRY/$NAMESPACE/$MODULE
        docker push $REGISTRY/$NAMESPACE/$MODULE

done

