#!/bin/bash

./createSSLStores.sh

docker-compose up -d

sleep 5

docker-compose up -d 



