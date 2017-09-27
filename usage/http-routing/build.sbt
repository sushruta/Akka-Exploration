name := "Akka - usage - http-routing"
version := "1.0"
scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  // "io.spray" %%  "spray-json" % "1.3.3",
  "com.typesafe.akka" % "akka-http-spray-json_2.11" % "10.0.10",

  "com.typesafe.akka" %% "akka-http" % "10.0.10",
  "com.typesafe.akka" %% "akka-actor" % "2.5.4",
  "com.typesafe.akka" %% "akka-stream" % "2.5.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.4" % Test
)
