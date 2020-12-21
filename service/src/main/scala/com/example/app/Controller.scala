package com.example.app

import akka.actor.{ActorSystem, Props}
import com.example.app.core.SparkService
import com.example.app.core.async.{EventProcessor, PredictRequestReader, PredictionWriter}
import org.scalatra._
import com.example.app.utils.AppConfig
import org.apache.log4j.{Level, Logger}
import org.json4s._
import org.json4s.jackson.Serialization.write
import org.slf4j.LoggerFactory

/**
 * This defines the routes and starts Spark and Akka Actors
 */
class Controller extends ScalatraServlet {

  implicit val formats: DefaultFormats.type = DefaultFormats
  import model.Responses._

  private val log = LoggerFactory.getLogger(getClass)
  Logger.getLogger("org").setLevel(Level.OFF)

  val system = ActorSystem("Bitcoin")

  val bootstrapServer: String = AppConfig.get("kafka.bootstrap.server")
  val predictRequestTopic: String = AppConfig.get("kafka.predictRequest.topic")
  val predictionTopic: String = AppConfig.get("kafka.prediction.topic")

  val eventProcessor = system.actorOf(Props(classOf[EventProcessor], new PredictRequestReader(bootstrapServer, predictRequestTopic), new PredictionWriter(bootstrapServer, predictionTopic)), "eventProcessor")

  //Require to start Spark on startup
  SparkService.spark

  get("/price") {
    val date = params("date")
    log.info(s"Received request to predict price on $date")
    val price = SparkService.getPrice(date)
    log.info(s"Predicted price $price on $date")
    write(Price(price))
  }

  get("/health") {
  }

  error {
    case e: Exception =>
      log.error("Unexpected error during http api call.", e)
      InternalServerError(write(Message("Internal server error")))
  }

  notFound {
    contentType = "application/json;charset=UTF-8"
    NotFound(write(Message("Not found")))
  }
}