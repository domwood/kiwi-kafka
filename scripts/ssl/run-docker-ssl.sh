#!/bin/bash

./stop-docker.sh
./createSSLStores.sh

docker-compose -f docker-compose.yml  up -d

sleep 5

docker-compose -f docker-compose.yml up -d



