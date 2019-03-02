#!/bin/bash

cd src/main/ui/
NODE_ENV="LOCAL" LOCAL_SPRING_API="http://localhost:8080/api/" npm start
