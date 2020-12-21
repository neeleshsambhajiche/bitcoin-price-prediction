name := "upstream"

version := "0.1"

scalaVersion := "2.11.8"

val sparkVersion = "2.3.1"
val kafkaVersion = "0.11.0.3"

libraryDependencies ++= Seq(
  "org.apache.kafka" %% "kafka" % kafkaVersion,
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.json4s" %% "json4s-jackson" % "3.2.11"
)