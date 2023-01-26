#!/bin/sh
set -e

echo "checking for ninja mode from env: $NINJA_MODE"
NINJA_MODE_TO_USE=${NINJA_MODE:-prod}
echo "will use ninja mode: $NINJA_MODE_TO_USE"

# gc logs  "-XX:+PrintGCDateStamps", "-verbose:gc", "-XX:+PrintGCDetails", 
java -Xms4096m -Xms4096m -server \
	-XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled \
	-XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=72 \
	-XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark \
	-Dninja.external.configuration=/app/application.conf \
	-Dninja.mode="$NINJA_MODE_TO_USE" \
	-jar bulibot.jar
