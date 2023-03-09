package com.matkob.msconfig

import cats._
import cats.implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._
import cats.kernel.Semigroup
import org.scalatest.OptionValues

class OpenConfigSuite extends AnyFlatSpec with Matchers with OptionValues {

  given [K, V]: Semigroup[Map[K, V]] = _ ++ _

  "OpenConfig" should "lazy evaluate overrides" in {
    val initial   = Map("test" -> "value")
    val newValues = Map("test" -> "new value")

    val config = OpenConfig[Map[String, String]](initial).withConfigOverride(newValues)

    config.config shouldEqual initial
    config.compile.config shouldEqual newValues
  }

  it should "gradually apply new overrides" in {
    val initial     = Map("test" -> "value")
    val newValues   = Map("test" -> "new value", "new key" -> "some value")
    val newerValues = Map("test" -> "even newer value")

    val compiled = OpenConfig[Map[String, String]](initial)
      .withConfigOverride(newValues)
      .withConfigOverride(newerValues)
      .compile

    compiled.config should contain("test" -> "even newer value")
    compiled.config should contain("new key" -> "some value")
  }

  it should "compile applying all overrides" in {
    val initial   = Map("test" -> "value")
    val newValues = Map("test" -> "new value")

    val compiled = OpenConfig[Map[String, String]](initial).withConfigOverride(newValues).compile

    compiled.config shouldEqual newValues
    compiled.overrides shouldBe empty
  }

  it should "transform to product" in {
    val initial                                   = Map("test" -> "value")
    given Conversion[Map[String, String], String] = _.keySet.head

    val value = OpenConfig[Map[String, String]](initial).value

    value shouldEqual OpenValue("test")
  }

  it should "transform to product given applicative conversion" in {
    val initial                                           = Map("test" -> "value")
    given Conversion[Map[String, String], Option[String]] = _.keySet.headOption
    given Applicative[Option]                             = cats.Invariant.catsInstancesForOption

    val value = OpenConfig[Map[String, String]](initial).valueF

    value.value shouldEqual OpenValue("test")
  }

  it should "merge initial config when combined" in {
    val initial   = Map("test" -> "value")
    val newValues = Map("test" -> "new value")

    val combined =
      OpenConfig[Map[String, String]](initial) |+| OpenConfig[Map[String, String]](newValues)

    combined.config shouldEqual newValues
  }

  it should "append left overrides with right ones when combined" in {
    val initial   = Map("test" -> "value")
    val newValues = Map("test" -> "new value")

    val left = OpenConfig[Map[String, String]](initial).withConfigOverride(Map.empty)
    val right = OpenConfig[Map[String, String]](newValues).withConfigOverride(Map("1" -> "2"))
    val combined = left |+| right

    combined.overrides should have size 2
    combined.overrides.head.apply(Map.empty) shouldEqual Map.empty
    combined.overrides.last.apply(Map.empty) shouldEqual Map("1" -> "2")
  }
}
