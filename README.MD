# KIWI - Kafka Interactive Web Interface

 - A Kafka Interface written by for real use cases when working with kafka
 - Differences from other UIs
    1) Focus on Rest API versions of console scripts provided in kafka installs bin dir
    2) Support for kafka headers
    3) Live streaming of data with filters
  
**Note: This Project is WIP, I've made this public so it I can use it at work.
 But it does not represent a final finished product, its not recommended to use in production at this time.**
  
  
#### Features

In general the target is to replicate many of the features of provided by the scripts which are included with the kafka server. 
Such as kafka-console-consumer.sh, kafka-consumer-groups.sh

- Examples: 

![Example Screen showing Topic View](./img/TopicView.png "Topic View")


![Example Screen showing Consumer View](./img/ConsumerView.png "Consumer View")

 
#### Run via Docker
//TODO

`docker run -p 8080:8080 domwood/kiwi`
 
#### Build & Run Jar
 
 - Run `mvn clean install`
 - or Jar runnable via `java -jar target/kiwi-$version.jar`
  
#### Development - Getting Started

There are various ways this can be started locally, this is my preferred method:

 - Run `./run-docker.sh`
 - Start KiwiApplication in your ide or via above method
 - Run `./run-node-server.sh`
 - Go to localhost:3000 to see UI
 
Editing should javascript should lead to live updates of the UI at localhost:3000.
The UI will also be available at `localhost:8080` or whatever port you have set server.port to.
But this will not update automatically when making changes.
  
