#!/bin/bash
source dockerHelper.sh
docker run -v /Users/chris/dev/sources/bulibot/jsonStorage:/data/jsonStorage -v /Users/chris/dev/sources/bulibot/mediaStorage:/data/media -p 8080:8080 --name $IMAGE -d $REPOSITORY/$IMAGE:$VERSION
