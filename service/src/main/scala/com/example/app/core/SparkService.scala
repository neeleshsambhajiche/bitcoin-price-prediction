package com.example.app.core

import java.sql.Timestamp
import java.text.SimpleDateFormat

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.functions.{datediff, dayofmonth, lit, month}
import org.apache.spark.sql.types.{StructField, StructType, TimestampType}
import org.apache.spark.sql.{Row, SparkSession}

/**
 * @since 20/12/20
 */
object SparkService {

  Logger.getLogger("org").setLevel(Level.OFF)

  val spark = SparkSession.builder
    .appName("Bitcoin-Model-Trainer")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  val format = new SimpleDateFormat("yyyy-MM-dd")
  val schema = List(
    StructField("Date", TimestampType, true)
  )

  val pipelineModel = PipelineModel.load("model")

  def getPrice(date: String): Double = {
    val d = format.parse(date)
    val t = new Timestamp(d.getTime())
    val data = Seq(Row(t))

    val df = spark.createDataFrame(
      spark.sparkContext.parallelize(data),
      StructType(schema)
    )

    var newDF = df.withColumn("row_number", datediff($"Date", lit("2019-12-19")))
    newDF = newDF.withColumn("month", month($"Date")).withColumn("monthday", dayofmonth($"Date"))


    val prediction = pipelineModel.transform(newDF)
    prediction.select("prediction").head.getDouble(0)
  }

}
