package com.example.app.core.async

import akka.actor.Actor
import akka.event.Logging
import com.example.app.core.SparkService
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse

import scala.collection.JavaConverters._
import scala.collection.mutable.Map
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class EventProcessor(predictRequestReader: PredictRequestReader, predictionWriter: PredictionWriter) extends Actor {

  import EventProcessor._
  val log = Logging(context.system, this)
  implicit val formats = DefaultFormats
  implicit val ec: ExecutionContextExecutor = context.dispatcher

  //Triggers after every 5 minutes
  val readKafkaTrigger = context.system.scheduler.schedule(5.second, 5.second, self, ReadKafka)

  override def postStop(): Unit = {
    readKafkaTrigger.cancel()
  }

  def receive = {

    case ReadKafka =>
      val records = predictRequestReader.consumer.poll(1000).asScala

      for (record <- records) {
        val event = parse(record.value()).extract[PredictEvent]
        val price = SparkService.getPrice(event.date)
        predictionWriter.writePredictionEvent(event.date, price)
        val offsets = Map[TopicPartition, OffsetAndMetadata]()
        offsets += (new TopicPartition(record.topic, record.partition) -> new OffsetAndMetadata(record.offset() + 1))
        predictRequestReader.consumer.commitSync(offsets.asJava)
      }

    case _      => log.info("received unknown message")
  }
}

object EventProcessor {
  case class PredictEvent(eventType: String, eventId: String, date: String)
  case object ReadKafka
}
