name := "files-fu"
version := "0.1"

scalaVersion := "2.13.6"

scalacOptions ++= Seq("-deprecation", "-feature")

val AkkaVersion = "2.6.14"
val AkkaHttpVersion = "10.2.6"
libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-influxdb" % "3.0.3",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test,
  )

configs(IntegrationTest)
Defaults.itSettings

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test,it"
libraryDependencies += "org.scalatestplus" %% "scalacheck-1-15" % "3.2.9.0" % "test,it"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.32" % "test,it"


enablePlugins(JavaAppPackaging)
