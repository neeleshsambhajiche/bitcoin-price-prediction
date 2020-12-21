package com.example.app.core.async

import akka.actor.{Actor, ActorLogging, Cancellable}
import com.example.app.core.SparkService
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import scala.collection.JavaConverters._
import scala.collection.mutable.Map
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

/**
 * This actor is used to handle prediction requests through Kafka asynchronously.
 * It polls Kafka every 30 seconds, processes events and writes prediction
 * results back to Kafka
 * @param predictRequestReader KafkaConsumer which reads predicts event
 * @param predictionWriter KafkaConsumer which writes prediction events
 */
class EventProcessor(predictRequestReader: PredictRequestReader, predictionWriter: PredictionWriter) extends Actor with ActorLogging {

  import EventProcessor._
  implicit val formats: DefaultFormats.type = DefaultFormats
  implicit val ec: ExecutionContextExecutor = context.dispatcher
  val actorName: String = self.path.name

  //Triggers after every 30 seconds
  val readKafkaTrigger: Cancellable = context.system.scheduler.schedule(5.second, 30.second, self, ReadKafka)

  override def postStop(): Unit = {
    readKafkaTrigger.cancel()
  }

  def receive = {

    //This triggers the Kafka consumer to poll Kafka and process the events
    case ReadKafka =>
      val records = predictRequestReader.consumer.poll(1000).asScala

      for (record <- records) {
        val event = parse(record.value()).extract[PredictEvent]
        val date = event.date
        log.debug(s"$actorName: Received message to predict price on $date")
        val price = SparkService.getPrice(date)
        log.debug(s"$actorName: Predicted price $price on $date")
        predictionWriter.writePredictionEvent(date, price)

        //Commit the offset for the processed event
        val offsets = Map[TopicPartition, OffsetAndMetadata]()
        offsets += (new TopicPartition(record.topic, record.partition) -> new OffsetAndMetadata(record.offset() + 1))
        predictRequestReader.consumer.commitSync(offsets.asJava)
      }

    case _      => log.info(s"$actorName: received unknown message")
  }
}

// Companion object is used to define the messages used by the Actor
object EventProcessor {
  case class PredictEvent(eventType: String, eventId: String, date: String)
  case object ReadKafka
}
