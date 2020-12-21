val ScalatraVersion = "2.6.3"

organization := "com.example"

name := "My Scalatra Web App"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers += Classpaths.typesafeReleases

val sparkVersion = "2.3.1"
val kafkaVersion = "0.11.0.3"
val AkkaVersion = "2.4.5"
val testcontainersScalaVersion = "0.36.0"

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % ScalatraVersion  ,
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime" ,
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.9.v20180320" % "container" ,
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"  ,
  "com.sun.jersey" % "jersey-core" % "1.19.4" ,
  "com.sun.jersey" % "jersey-server" % "1.19.4" ,
  "org.apache.spark" % "spark-core_2.11" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.kafka" %% "kafka" % kafkaVersion exclude("net.jpountz.lz4", "lz4"),
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test" ,
  "com.dimafeng" %% "testcontainers-scala-scalatest" % testcontainersScalaVersion % "test",
  "com.dimafeng" %% "testcontainers-scala-kafka" % testcontainersScalaVersion % "test",
  "org.scalatestplus" %% "mockito-3-4" % "3.2.2.0" % "test"
)

enablePlugins(ScalatraPlugin)
