Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "3.2.1"
ThisBuild / organization := "com.matkob.msconfig"
ThisBuild / version      := "0.1.0-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .settings(
    name := "ms-config",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"                % Version.Cats,
      "com.typesafe"   % "config"                   % Version.TypeSafeConfig,
      "com.monovore"  %% "decline"                  % Version.Decline,
      "co.fs2"        %% "fs2-core"                 % Version.Fs2,
      "co.fs2"        %% "fs2-io"                   % Version.Fs2,
      "org.scalatest" %% "scalatest-flatspec"       % Version.ScalaTest % Test,
      "org.scalatest" %% "scalatest-shouldmatchers" % Version.ScalaTest % Test
    )
  )

addCommandAlias("fmt", ";scalafmtAll;scalafmtSbt")
