package com.example.app.core.async

import java.time.Duration
import java.util.Properties

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.duration.DurationInt


class PredictRequestReader(bootstrapServers: String, topic: String) {

  val kafkaConsumerProps: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", bootstrapServers)
    props.put("key.deserializer", classOf[StringDeserializer].getName)
    props.put("value.deserializer", classOf[StringDeserializer].getName)
    props.put("group.id", "anongroup")
    props.put("enable.auto.commit", "false")
    props.put("auto.offset.reset", "earliest")
    props
  }

  val pollDuration: Duration = java.time.Duration.ofMillis(1.seconds.toMillis)

  val consumer = new KafkaConsumer[String, String](kafkaConsumerProps)
  consumer.subscribe(java.util.Arrays.asList(topic))

//  while(true) {
//    val records  = consumer.poll(pollDuration).asScala
//    for( record <- records) {
//      println(parse(record.value()).extract[Event].date)
//    }
//  }



}
