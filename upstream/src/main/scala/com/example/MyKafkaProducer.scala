package com.example

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write


object MyKafkaProducer extends App {

  implicit val formats = DefaultFormats
  case class Event(eventType: String, eventId: String, date: String)

  val kafkaProducerProps: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", classOf[StringSerializer].getName)
    props.put("value.serializer", classOf[StringSerializer].getName)
    props
  }

  val topic = "bitcoin"

  val producer = new KafkaProducer[String, String](kafkaProducerProps)
  val record = new ProducerRecord[String, String](topic, write(Event("bitcoinPricePredictionRequest", java.util.UUID.randomUUID.toString, "2020-12-22")))
  producer.send(record)
  producer.close()

}
