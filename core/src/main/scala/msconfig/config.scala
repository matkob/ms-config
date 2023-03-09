package com.matkob.msconfig

import monocle.syntax.all._
import monocle.AppliedLens
import cats.kernel.Semigroup
import cats.implicits._

trait CmdParameters {
  def get(name: String): Option[String]
}

trait EnvVariables {
  def get(name: String): Option[String]
}

type LazyFocus[T, V] = T => AppliedLens[T, V]

class ComposedConfig[Config, Product](
    config: Config,
    configOverrides: Seq[Config => Config] = Seq.empty,
    productOverrides: Seq[Product => Product] = Seq.empty
) {

  def compile(decoder: Config => Product): Product = {
    val cfg = configOverrides.foldLeft(config)((acc, func) => func(acc))
    productOverrides.foldLeft(decoder(cfg))((acc, func) => func(acc))
  }

  def withCliOverride[Replacement](
      name: String,
      focus: LazyFocus[Product, Replacement]
  )(using
      c: Conversion[String, Replacement],
      cmd: CmdParameters
  ): ComposedConfig[Config, Product] = {
    cmd.get(name) match
      case None        => ???
      case Some(value) => withStringOverride(value, focus)
  }

  def withEnvOverride[Replacement](
      name: String,
      focus: LazyFocus[Product, Replacement]
  )(using
      c: Conversion[String, Replacement],
      env: EnvVariables
  ): ComposedConfig[Config, Product] = {
    env.get(name) match
      case None        => ???
      case Some(value) => withStringOverride(value, focus)
  }

  def withStringOverride[Replacement](
      value: String,
      focus: LazyFocus[Product, Replacement]
  )(using Conversion[String, Replacement]): ComposedConfig[Config, Product] =
    withOverride(focus, value)

  def withConfigOverride(cfg: Config)(using
      Semigroup[Config]
  ): ComposedConfig[Config, Product] =
    ComposedConfig(
      config,
      configOverrides :+ (c => c |+| cfg),
      productOverrides
    )

  def withOverride[Replacement](
      focus: LazyFocus[Product, Replacement],
      value: Replacement
  ): ComposedConfig[Config, Product] =
    ComposedConfig(
      config,
      configOverrides,
      productOverrides :+ (c => focus(c).replace(value))
    )
}
