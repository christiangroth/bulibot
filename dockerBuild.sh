#!/bin/bash
source dockerHelper.sh

# prepare docker resources
JAR_FILE=`ls target/bulibot*.jar`
mkdir -p target/docker
cp $JAR_FILE target/docker/bulibot.jar
cp -r src/main/docker/* target/docker/

# create docker image
docker build -t $REPOSITORY/$IMAGE:$VERSION .
