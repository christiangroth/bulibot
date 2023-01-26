#!/bin/bash
source dockerHelper.sh
docker stop $IMAGE
docker rm $IMAGE
