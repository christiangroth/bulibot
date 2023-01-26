#!/bin/sh
set -e

wait_file() {
  local file="$1"; shift
  local wait_seconds="$1"; shift 
  until test $((wait_seconds--)) -eq 0 -o -e "$file" ; do sleep 1; done

  # ((++wait_seconds))
}

# Wait at most 30 seconds for the marker file to appear
copy_finished=/data/COPY_FINISHED
wait_file "$copy_finished" 30 || {
  echo "Copy finished marker file missing after waiting for $? seconds: '$copy_finished'"
  exit 1
}

# cleanup marker and go on with regular start
rm -f $copy_finished
./entrypoint.sh
