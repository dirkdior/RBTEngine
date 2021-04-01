name         := "rtbengine"

version      := "0.1"

scalaVersion := "2.13.5"

lazy val akkaVersion      = "2.6.13"
lazy val akkaHttpVersion  = "10.2.4"
lazy val scalaTestVersion = "3.2.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-testkit"         % akkaVersion      % Test,
  "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion      % Test,
  "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion  % Test,
  "org.scalatest"     %% "scalatest"            % scalaTestVersion % Test
)