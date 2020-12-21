package com.tookitaki.bitcoin.model

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.VectorIndexer
import org.apache.spark.ml.regression.{RandomForestRegressionModel, RandomForestRegressor}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.feature.OneHotEncoderEstimator
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.regression.{LinearRegression,RandomForestRegressor}
import org.apache.spark.sql.Row
import org.apache.spark.ml.tuning.ParamGridBuilder
import org.apache.spark.ml.tuning.CrossValidator
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.OneHotEncoderEstimator

object ModelTrainer {

  Logger.getLogger("org").setLevel(Level.OFF)

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder
      .appName("Bitcoin-Model-Trainer")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Load and parse the data file, converting it to a DataFrame.
    val data = spark.read.option("header", true)
      .option("inferSchema","true")
      .csv("data/BTC-USD.csv")
      .select("Date","Close")
      .withColumn("row_number", datediff($"Date", lit("2019-12-19")))
      .withColumn("month", month($"Date"))
      .withColumn("monthday", dayofmonth($"Date"))

    val trainData = data.filter($"row_number" < 300)
    val testData = data.filter($"row_number" >= 300)

    //Encode month and monthday columns
    val encoder = new OneHotEncoderEstimator()
      .setInputCols(Array("month", "monthday"))
      .setOutputCols(Array("monthVec", "monthdayVec"))

    //Assemble all the features into a single vector column
    val assembler = new VectorAssembler()
      .setInputCols(Array("row_number", "monthVec", "monthdayVec"))
      .setOutputCol("features")

    // Train a RandomForest model.
    val rf = new RandomForestRegressor()
      .setLabelCol("Close")
      .setFeaturesCol("features")

    // Chain encoder, assembler and forest in a Pipeline.
    val pipeline = new Pipeline().setStages(Array(encoder, assembler, rf))

    // Train model
    val model = pipeline.fit(trainData)

    //Save model
    model.write
      .overwrite()
      .save("model")

    // Make predictions
    val predictions = model.transform(testData)

    // Select example rows to display.
    predictions.select("prediction", "Close", "features").show(5)

    // Select (prediction, true label) and compute test error.
    val evaluator = new RegressionEvaluator()
      .setLabelCol("Close")
      .setPredictionCol("prediction")
      .setMetricName("rmse")
    val rmse = evaluator.evaluate(predictions)
    println(s"Root Mean Squared Error (RMSE) on test data = $rmse")

    //val rfModel = model.stages(1).asInstanceOf[RandomForestRegressionModel]
    //println(s"Learned regression forest model:\n ${rfModel.toDebugString}")
  }
}
