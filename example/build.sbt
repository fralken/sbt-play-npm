name := """example"""
organization := "com.example"

version := "2.0"

lazy val root = (project in file("."))
  .settings(Seq(npmSrcDir := "spa"))
  .enablePlugins(PlayScala)

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
