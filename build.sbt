name := "mayank_k_rastogi_cs474_hw3"

version := "0.1"

scalaVersion := "2.13.1"

scalacOptions += "-target:jvm-1.11"

libraryDependencies ++= Seq(

  // Logback logging framework
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.gnieh" % "logback-config" % "0.4.0",

  // Scalatest testing framework
  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)
