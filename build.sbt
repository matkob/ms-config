Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "3.2.1"
ThisBuild / organization := "com.matkob.msconfig"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalacOptions ++= Seq("-feature", "-language:implicitConversions")

lazy val root = (project in file("."))
  .aggregate(core)

lazy val core = project.in(file("core"))

addCommandAlias("fmt", ";scalafmtAll;scalafmtSbt")
