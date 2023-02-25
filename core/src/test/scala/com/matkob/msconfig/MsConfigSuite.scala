package com.matkob.msconfig

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._
import monocle.AppliedLens
import monocle.syntax.all._
import cats.kernel.Semigroup
import cats.syntax.all._

class MsConfigSuite extends AnyFlatSpec with Matchers {

  "MsConfig" should "override chosen values" in {
    case class SomeClass(value: String)

    val config = Map("test" -> "value1")
    val focusFunc: SomeClass => AppliedLens[SomeClass, String] =
      c => c.focus(_.value)

    given Semigroup[Map[String, String]] = (a, b) => a ++ b
    given Conversion[String, String]     = identity
    given CmdParameters                  = _ => "cmdValue".some
    given EnvVariables                   = _ => "envValue".some

    val composed = ComposedConfig[Map[String, String], SomeClass](config)
      .withCliOverride("test.value.path", focusFunc)
      .withEnvOverride("TEST_ENV", focusFunc)
      .withConfigOverride(Map("test" -> "newValue"))

    val result = composed.compile(c => SomeClass(c("test")))

    result.value shouldEqual "envValue"
  }
}
