name := "files-fu"
version := "0.1"

scalaVersion := "2.13.6"

scalacOptions ++= Seq("-deprecation", "-feature")

configs(IntegrationTest)
Defaults.itSettings

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test,it"
libraryDependencies += "org.scalatestplus" %% "scalacheck-1-15" % "3.2.9.0" % "test,it"

libraryDependencies += "io.kamon" %% "kamon-core" % "2.3.1"
libraryDependencies += "io.kamon" %% "kamon-status-page" % "2.3.1"
libraryDependencies += "io.kamon" %% "kamon-apm-reporter" % "2.3.1"
libraryDependencies += "io.kamon" %% "kamon-influxdb" % "2.3.1"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.32" % "test,it"


enablePlugins(JavaAppPackaging)
