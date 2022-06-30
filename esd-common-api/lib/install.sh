#!/bin/bash

VERSION="1.4.0"
mvn install:install-file \
    -Dfile="./api-spec-$VERSION-SNAPSHOT.jar" \
    -DgroupId='org.iii.dnp3' \
    -DartifactId='api-spec' \
    -Dversion="$VERSION-SNAPSHOT" \
    -Dpackaging='jar' \
    -DgeneratePom=true
