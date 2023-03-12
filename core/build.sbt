name := "core"
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core"                % Version.Cats,
  "dev.optics"    %% "monocle-core"             % Version.Monocle,
  "dev.optics"    %% "monocle-macro"            % Version.Monocle,
  "org.scalatest" %% "scalatest-flatspec"       % Version.ScalaTest % Test,
  "org.scalatest" %% "scalatest-shouldmatchers" % Version.ScalaTest % Test
)
