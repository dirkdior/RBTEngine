name         := "rtbengine"

version      := "0.1"

scalaVersion := "2.13.5"

lazy val akkaHttpVersion = "10.2.4"
lazy val akkaVersion     = "2.6.13"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka" %% "akka-stream"          % akkaVersion
)