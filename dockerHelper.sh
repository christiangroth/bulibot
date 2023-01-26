#!/bin/bash
VERSION=`ls target/bulibot*.jar | sed "s/target\/bulibot-\(.*\)\(-SNAPSHOT\)\{0,1\}\.jar/\1/"`
REPOSITORY=chrgroth
IMAGE=bulibot
