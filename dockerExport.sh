#!/bin/bash
source dockerHelper.sh
docker save --output ../$IMAGE-$VERSION.tar $REPOSITORY/$IMAGE:$VERSION
