# KIWI - Kafka Interactive Web Interface

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A Kafka Web Interface, written to help my professional day to day role working with kafka, but provided here in the event anyone else may benefit from using it.

#### What this tool attempts to provide
 - API versions of console scripts provided in kafka installs bin dir
 - Support for kafka headers
 - Live streaming of data with filters
  

#### What this tool is
 - Project is a springboot app using reactjs frontend.
 - Project has been development against kafka >2.0.0. Whilst I intend to look at backwards compatibility with older kafka versions. 
 It have not looked into it yet.
  
  
## Overview

In general the target is to replicate many of the features of provided by the scripts which are included with the kafka server. 
Such as kafka-console-consumer.sh, kafka-consumer-groups.sh

- Example Screenshots: 

![Example Screen showing Topic View](./img/TopicView.png "Topic View")


![Example Screen showing Consumer View](./img/ConsumerView.png "Consumer View")


## Getting Started

#### Running via Docker

`docker run -p 8080:8080 dmwood/kiwi`
 
#### Build & Run Jar

Dependencies: 
 - Requires Java 8 or higher 
 - Requires Node 8 or higher
 - Requires Maven 3 or higher
 
Build:
 - Run `mvn clean install`
 - or Jar runnable via `java -jar target/kiwi-$version.jar`
  
#### Development - Getting Started

There are various ways this can be started locally, this is my preferred method:

 - Run `./run-docker.sh`
 - Start KiwiApplication in your ide or via above method
 - Run `./run-node-server.sh`
 - Go to `localhost:3000` to see UI
 
Editing should javascript should lead to live updates of the UI at `localhost:3000`.
The UI will also be available at `localhost:8080` or whatever port you have set `server.port` to.
But this will not update automatically when making changes.
  
