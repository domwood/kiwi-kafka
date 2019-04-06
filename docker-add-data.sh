#!/bin/bash

topic=$1


if [ "$topic" == "" ]; then
	echo "No topic specified"
else
	docker exec -i kafka1 /opt/kafka/bin/kafka-producer-perf-test.sh --topic $topic --throughput 10 --num-records 300 --record-size 2048 --producer-props bootstrap.servers=kafka1:9092
fi
