# KIWI - Kafka Interactive Web Interface

 - A Kafka Interface written by for real use cases when working with kafka
 - Differences from other UIs
    1) Focus on Rest API versions of console scripts provided in kafka installs bin dir
    2) Support for kafka headers
    3) Live streaming of data with filters
 
##### Development - Getting Started

 - Start KiwiApplication in your ide
 - Run `./run-node-server.sh`
 - Run `./run-docker.sh`
 
  
##### Build & Run Jar

 - Run `mvn clean install`
 - Jar runnable via `java -jar target/kiwi-$version.jar`
 

### Milestones

 - Ordering flexible 

##### M1
 - [X] Basic Producer Page
 - [X] Basic Consumer Page
 - [ ] Basic Create Topics Page
 - [ ] Basic View Topics Page
 - [ ] Reasonable Java Unit Test Coverage

##### M2
 - [ ] Better Java Unit Test Coverage
 - [ ] Reasonable Javascript Unit Test Coverage
 - [X] Toast Alerts
 - [ ] Websocket Streaming for Consumer Page
 
##### M3
 - [ ] Support for none-string consumer/producers
 - [ ] Caching kafka resources
 - [ ] Kafka Brokers/ Zookeeper Page
 - [ ] API documentation
 - [ ] Integration Tests with real kafka (container tests?)
 
##### M4
 - [ ] Modularisation for other kafka client versions
 - [ ] Support for other kafka versions
 - [ ] Zookeeper inspection
 - [ ] Basic diagnostics/health/inspection of brokers
 