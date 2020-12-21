# Bitcoin Price Prediction 
This application allows users get a predict the bitcoin price given a date using both Rest API endpoint as well as Kafka events.

## Modules ##
Note: This project does not contain a multi module sbt set up. To open in Intellij, open each module separately.

### trainer 
This contains the logic for building the prediction model using SparkML.
Open source bitcoin data is used which is stored in the `data` folder
Post running the code, the model will be stored in `model` folder. This needs to be copied to the `service` module before starting the service.

Note: The service module already contains an existing model

### service ###
It exposes the price endpoint via a rest service. Additionally, predict request events from Kafka are processed asynchronously and the prediction price is written back to Kafka.

### external ###
This contains helper objects for writing to and reading from Kafka.

## Setting Kafka ##
This project has a external dependency on Kakfa. 

Follow http://kafka.apache.org/quickstart to download Kafka and start zookeeper and kafka server

Default topic for predict request events: `bitcoin`
Default topic for prediction response events: `bitcoin-price`

```sh
bin/kafka-topics.sh --create --topic bitcoin --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic bitcoin-price --bootstrap-server localhost:9092
```

Start the service. 

Use the `MyKafkaProducer` in the `external` module to write
predict request events to Kafka. 

Use the `MyKafkaConsumer` to read the prediction response event with the bitcoin price. 

## Run Service ##

To start the service
```sh
$ cd service
$ sbt
> jetty:start
```
To stop the service
```sh
> jetty:stop
```

To test, try out a sample query
```sh
curl http://localhost:8080/price?date=2020-12-19
```
## Test ##

To run the test cases:
```sh
sbt test
```