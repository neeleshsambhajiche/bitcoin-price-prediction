package com.example.app

import akka.actor.{ActorSystem, Props}
import com.example.app.core.SparkService
import com.example.app.core.async.{EventProcessor, PredictRequestReader, PredictionWriter}
import org.scalatra._
import com.example.app.utils.AppConfig
import org.json4s._
import org.json4s.jackson.Serialization.write

class Controller extends ScalatraServlet {

  implicit val formats = DefaultFormats
  import model.Responses._

  val system = ActorSystem("Bitcoin")

  val bootstrapServer: String = AppConfig.get("kafka.bootstrap.server")
  val predictRequestTopic = AppConfig.get("kafka.predictRequest.topic")
  val predictionTopic = AppConfig.get("kafka.prediction.topic")

  val eventProcessor = system.actorOf(Props(classOf[EventProcessor], new PredictRequestReader(bootstrapServer, predictRequestTopic), new PredictionWriter(bootstrapServer, predictionTopic)), "eventProcessor")

  //Require to start Spark on startup
  SparkService.spark

  get("/price") {
    val date = params("date")
    write(Price(SparkService.getPrice(date)))
  }

  get("/health") {
  }

}
