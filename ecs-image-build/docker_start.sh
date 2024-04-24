#!/bin/bash
#
# Start script for disqualified-officers-data-api

PORT=8080

exec java -jar -Dserver.port="${PORT}" "disqualified-officers-data-api.jar"
