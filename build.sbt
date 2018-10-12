lazy val akkaHttpVersion = "10.1.5"
lazy val akkaVersion = "2.5.17"

lazy val root = (project in file(".")).settings(
  inThisBuild(List(organization := "org.alexeyn", scalaVersion := "2.12.7")),
  name := "crud-service",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.0.5" % Test
  )
)
