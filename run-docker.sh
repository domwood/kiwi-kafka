#!/bin/bash

HOST=$(ifconfig | grep "inet addr\:" | grep "addr:[1][0,9]" | sed -r 's/ +/ /g' | cut -d " " -f3 | grep -o "[0-9.]*")


echo "Local host ip address: $HOST"

export LOCALHOSTNAME="$HOST"

echo $LOCALHOSTNAME

docker-compose up -d

