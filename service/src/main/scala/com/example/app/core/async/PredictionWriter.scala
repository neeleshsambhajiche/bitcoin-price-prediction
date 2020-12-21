package com.example.app.core.async

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.json4s._
import org.json4s.jackson.Serialization.write

class PredictionWriter(bootstrapServers: String, topic: String) {

  implicit val formats = DefaultFormats
  import PredictionWriter._

  val kafkaProducerProps: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", bootstrapServers)
    props.put("key.serializer", classOf[StringSerializer].getName)
    props.put("value.serializer", classOf[StringSerializer].getName)
    props
  }

  val producer = new KafkaProducer[String, String](kafkaProducerProps)

  def writePredictionEvent(date: String, price: Double) {
    val record = new ProducerRecord[String, String](topic, write(PredictionEvent("bitcoinPricePredictionResponse", java.util.UUID.randomUUID.toString, date, price)))
    producer.send(record)
  }
}

object PredictionWriter {
  case class PredictionEvent(eventType: String, eventId: String, date: String, price: Double)
}
