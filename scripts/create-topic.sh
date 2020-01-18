#!/bin/bash

topic=$1


if [ "$topic" == "" ]; then
	echo "No topic specified"
else
	docker exec -i kafka1 /opt/kafka/bin/kafka-topics.sh --create --topic $topic --zookeeper zookeeper:2181 --partitions 10 --replication-factor 3
fi

