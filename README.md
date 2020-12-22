# Bitcoin Price Prediction 
This application allows users get a predict the bitcoin price on a given date using both Rest API endpoint as well as asynchronously using Kafka events.

## Modules ##
Note: This project does not contain a multi module sbt set up. To open in Intellij, open each module separately.

### trainer 
This contains the logic for building the prediction model using SparkML.

Open source bitcoin data is used which is stored in the `data` folder

Post running the code, the model will be stored in `model` folder. This needs to be copied to the `service` module before starting the service.

Note: The service module already contains an existing model

### service ###
It exposes the price endpoint via a rest service. Additionally, predict request events from Kafka are processed asynchronously and the prediction price is written back to Kafka.

Swagger Documentation for the project is defined in `api.yml`

### external ###
This contains helper objects for writing to and reading from Kafka. It represents the upstream and downstream processes.

## Setting Kafka ##
This project has a external runtime dependency on Kakfa. Testing does not require Kafka as while running integration tests a Kafka container is provided using testcontainers library (https://github.com/testcontainers/testcontainers-scala)

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

Use the `MyKafkaConsumer` in the `external` module to read the prediction response event with the bitcoin price. 

### Events ###

Sample Predict Request Event
```sh
{
    "eventType": "bitcoinPricePredictionRequest",
    "eventId": "a2dab45d-221e-4aff-9fe1-4f9d1568ec25",
    "date": "2020-12-30"
}
```

Sample Prediction Response Event
```sh
{
    "eventType": "bitcoinPricePredictionResponse",
    "eventId": "a2dab45d-221e-4aff-9fe1-4f9d1568ec25",
    "date": "2020-12-30",
    "price": 9352.13
}
```

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