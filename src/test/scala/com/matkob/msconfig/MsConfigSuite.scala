package com.matkob.msconfig

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._
import cats.kernel.Monoid
import com.typesafe.config.Config

class MsConfigSuite extends AnyFlatSpec with Matchers {

  def overwritingMonoid[K, V] = new Monoid[Map[K, V]] {
    def empty                               = Map.empty
    def combine(x: Map[K, V], y: Map[K, V]) = x ++ y
  }

  "MsConfig" should "respect order of given config sources" in {
    val defaultConfig  = Map(1 -> 1)
    val specificConfig = Map(1 -> 3)

    val config = MsConfig(Seq(defaultConfig, specificConfig))
    config.compile(using overwritingMonoid[Int, Int]) shouldEqual specificConfig
  }

  it should "overwrite only duplicate values" in {
    val defaultConfig  = Map(1 -> 1, 2 -> 2)
    val specificConfig = Map(1 -> 3)

    val config = MsConfig(Seq(defaultConfig, specificConfig))
    config.compile(using overwritingMonoid[Int, Int]) should contain(2 -> 2)
  }
}
