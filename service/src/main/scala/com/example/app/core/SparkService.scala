package com.example.app.core

import java.sql.Timestamp
import java.text.SimpleDateFormat
import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.functions.{datediff, dayofmonth, lit, month}
import org.apache.spark.sql.types.{StructField, StructType, TimestampType}
import org.apache.spark.sql.{Row, SparkSession}

/**
 * This is the interface to spark. It defines the spark session and processed
 * predict queries
 */
object SparkService {

  val spark = SparkSession.builder
    .appName("Bitcoin-Predict")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  val format = new SimpleDateFormat("yyyy-MM-dd")
  val schema = List(
    StructField("Date", TimestampType, true)
  )

  // Load the model
  val pipelineModel = PipelineModel.load("model")

  /**
   * This returns the bitcoin price on a specific date
   */
  def getPrice(date: String): Double = {
    val d = format.parse(date)
    val timestamp = new Timestamp(d.getTime())
    val data = Seq(Row(timestamp))

    // Create the input df
    val df = spark.createDataFrame(
      spark.sparkContext.parallelize(data),
      StructType(schema)
    )

    // Add additional columns required for prediction
    var newDF = df.withColumn("row_number", datediff($"Date", lit("2019-12-19")))
    newDF = newDF.withColumn("month", month($"Date")).withColumn("monthday", dayofmonth($"Date"))

    // Predict
    val prediction = pipelineModel.transform(newDF)
    prediction.select("prediction").head.getDouble(0)
  }

}
