#!/usr/bin/env bash
echo 'Pull latest'
branch_name=$(git symbolic-ref -q HEAD)
git pull origin $branch_name

echo 'Build Packages'
mvn clean package -DskipTests

echo 'Put Files'
cp ./esd-server/target/esd-server-latest.jar	./esd-deploy/gembook/gembook-services/server/esd-server-latest.jar
cp ./esd-auth/target/esd-auth-latest.jar	./esd-deploy/gembook/gembook-services/auth/esd-auth-latest.jar
cp ./esd-client/target/esd-client-latest.jar	./esd-deploy/gembook/gembook-services/client/esd-client-latest.jar

