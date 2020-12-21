package com.example.app.integration

import java.util.{Properties, UUID}

import akka.actor.{ActorSystem, Props}
import com.dimafeng.testcontainers.{ForAllTestContainer, KafkaContainer}
import com.example.app.core.async.PredictionWriter.PredictionEvent
import com.example.app.core.SparkService
import com.example.app.core.async.{EventProcessor, PredictRequestReader, PredictionWriter}
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.log4j.{Level, Logger}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write
import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.LoggerFactory


class KafkaTestSpec extends FlatSpec with ForAllTestContainer with Matchers {

  private val log = LoggerFactory.getLogger(getClass)
  Logger.getLogger("org").setLevel(Level.OFF)

  override val container = KafkaContainer()
  container.start()
  implicit val formats = DefaultFormats
  case class Event(eventType: String, eventId: String, date: String)

  SparkService.spark

  val kafkaProducerProps: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", container.bootstrapServers)
    props.put("key.serializer", classOf[StringSerializer].getName)
    props.put("value.serializer", classOf[StringSerializer].getName)
    props
  }

  val producer = new KafkaProducer[String, String](kafkaProducerProps)

  val kafkaConsumerProps: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", container.bootstrapServers)
    props.put("key.deserializer", classOf[StringDeserializer].getName)
    props.put("value.deserializer", classOf[StringDeserializer].getName)
    props.put("group.id", UUID.randomUUID().toString)
    props.put("enable.auto.commit", "false")
    props.put("auto.offset.reset", "earliest")
    props
  }

  val consumer = new KafkaConsumer[String, String](kafkaConsumerProps)

  val predictRequestTopic = "messages-" + UUID.randomUUID()
  val predictionTopic = "messages-" + UUID.randomUUID()
  consumer.subscribe(java.util.Arrays.asList(predictionTopic))

  //Create Actor System
  val system = ActorSystem("Bitcoin-Test")

  val eventProcessor = system.actorOf(Props(classOf[EventProcessor], new PredictRequestReader(container.bootstrapServers, predictRequestTopic), new PredictionWriter(container.bootstrapServers, predictionTopic)), "eventProcessor")

  producer.send(new ProducerRecord[String, String](predictRequestTopic, write(Event("bitcoinPricePredictionRequest", java.util.UUID.randomUUID.toString, "2020-12-22")))).get()

  //Delay added for spark prediction
  Thread.sleep(15000)

  val records = consumer.poll(1000)

  it should "write a single prediction event" in {
    records.count() shouldEqual 1
  }

  it should "match the price" in {
    val record = records.iterator().next()
    val event = parse(record.value()).extract[PredictionEvent]
    event.price shouldEqual 10295.566803629315
  }

}
