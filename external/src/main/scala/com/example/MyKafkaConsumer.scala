package com.example

import java.time.Duration
import java.util.Properties
import scala.collection.JavaConverters._
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import scala.collection.mutable.Map


/**
 * Sample kafka consumer to read prediction event
 */
object MyKafkaConsumer extends App {

  val topic = "bitcoin-price"
  implicit val formats: DefaultFormats.type = DefaultFormats

  val kafkaConsumerProps: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.deserializer", classOf[StringDeserializer].getName)
    props.put("value.deserializer", classOf[StringDeserializer].getName)
    props.put("group.id", "anongroup")
    props.put("enable.auto.commit", "false")
    props.put("auto.offset.reset", "earliest")
    props
  }

  val pollDuration: Duration = java.time.Duration.ofMillis(1000)

  val consumer = new KafkaConsumer[String, String](kafkaConsumerProps)
  consumer.subscribe(java.util.Arrays.asList(topic))

  case class PredictionEvent(eventType: String, eventId: String, date: String, price: Double)

  while(true) {
    val records = consumer.poll(1000).asScala

    for (record <- records) {
      val event = parse(record.value()).extract[PredictionEvent]
      val date = event.date
      val price = event.price
      println(s"Date: $date -> Price: $price")

      //Commit the offset for the processed event
      val offsets = Map[TopicPartition, OffsetAndMetadata]()
      offsets += (new TopicPartition(record.topic, record.partition) -> new OffsetAndMetadata(record.offset() + 1))
      consumer.commitSync(offsets.asJava)
    }

  }
}
