package com.matkob.msconfig

import cats._
import cats.implicits._
import monocle.syntax.all._
import cats.kernel.Semigroup
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._

class OpenValueSuite extends AnyFlatSpec with Matchers {

  given CmdParameters           = _ => "1".some
  given EnvVariables            = _ => "1".some
  given Conversion[String, Int] = _ => 1

  given [K, V]: Semigroup[Map[K, V]] = _ ++ _

  "OpenValue" should "lazy evaluate overrides" in {
    val initial                              = Map(1 -> 2)
    val focus: LazyFocus[Map[Int, Int], Int] = map => map.focus(_.index(1))
    val value = OpenValue(initial).withCmdOverride("key", focus).withEnvOverride("key", focus)

    value.value shouldEqual initial
    value.compile.value shouldEqual Map(1 -> 1)
  }

  it should "gradually apply new overrides" in {
    val initial                                        = Map(1 -> 2, 2 -> 3, 3 -> 4)
    def focus(key: Int): LazyFocus[Map[Int, Int], Int] = map => map.focus(_.index(key))
    val compiled = OpenValue(initial)
      .withCmdOverride("key", focus(1))
      .withEnvOverride("key", focus(2))
      .compile

    compiled.value should contain(1 -> 1)
    compiled.value should contain(2 -> 1)
    compiled.value should contain(3 -> 4)
  }

  it should "compile applying all overrides" in {
    val initial                              = Map(1 -> 2)
    val focus: LazyFocus[Map[Int, Int], Int] = map => map.focus(_.index(1))
    val compiled                             = OpenValue(initial).withCmdOverride("key", focus).compile

    compiled.value shouldEqual Map(1 -> 1)
    compiled.overrides shouldBe empty
  }

  it should "merge initial value when combined" in {
    val initial   = Map(1 -> 2)
    val newValues = Map(2 -> 3)
    val combined  = OpenValue(initial) |+| OpenValue(newValues)

    combined.value shouldEqual Map(1 -> 2, 2 -> 3)
  }

  it should "append left overrides with right ones when combined" in {
    val initial                              = Map(1 -> 2)
    val newValues                            = Map(2 -> 3)
    val focus: LazyFocus[Map[Int, Int], Int] = map => map.focus(_.index(1))
    val left                                 = OpenValue(initial).withCmdOverride("key", focus)
    val right                                = OpenValue(newValues).withCmdOverride("key", focus)
    val combined                             = left |+| right

    combined.overrides should have size 2
    combined.overrides.head.apply(Map(1 -> 2)) shouldEqual Map(1 -> 1)
    combined.overrides.last.apply(Map(1 -> 2)) shouldEqual Map(1 -> 1)
  }
}
