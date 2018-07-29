organization := "ru.dayneko"

name := "outer-authorization"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "javax.servlet" % "javax.servlet-api" % "3.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe" % "config" % "1.2.1",
  "io.spray" %% "spray-json" % "1.3.2",
  "com.typesafe.akka" %% "akka-stream" % "2.5.13",
  "com.typesafe.akka" %% "akka-http" % "10.1.3",
  "org.apache.httpcomponents" % "httpclient" % "4.5.6",
  "commons-io" % "commons-io" % "2.5",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "junit" % "junit" % "4.10" % Test
)

enablePlugins(TomcatPlugin)

webappWebInfClasses := true