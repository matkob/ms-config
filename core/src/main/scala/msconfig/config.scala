package com.matkob.msconfig

import monocle.syntax.all._
import monocle.AppliedLens
import cats.kernel.Semigroup
import cats.implicits._

case class ComposedConfig[Config, Clazz](
    config: Config,
    // TODO: allow for mixed config, value override order, zip with index or different impl
    configOverrides: List[Config => Config] = Nil,
    valueOverrides: List[Clazz => Clazz] = Nil
) {
  def compile(decoder: Config => Clazz): Clazz = {
    val cfg = configOverrides.foldLeft(config)((acc, func) => func(acc))
    valueOverrides.foldLeft(decoder(cfg))((acc, func) => func(acc))
  }
}

type LazyFocus[T, V] = T => AppliedLens[T, V]

extension [Config, Clazz](config: ComposedConfig[Config, Clazz]) {
  def withCliOverride[T](
      name: String,
      focus: LazyFocus[Clazz, T]
  )(using Conversion[String, T]): ComposedConfig[Config, Clazz] = {
    config.withStringOverride(name, "cmdParam", focus)
  }

  def withEnvOverride[T](
      name: String,
      focus: LazyFocus[Clazz, T]
  )(using Conversion[String, T]): ComposedConfig[Config, Clazz] =
    config.withStringOverride(name, "envValue", focus)

  def withStringOverride[T](
      name: String,
      value: String,
      focus: LazyFocus[Clazz, T]
  )(using Conversion[String, T]): ComposedConfig[Config, Clazz] =
    config.withOverride(focus, value)

  def withConfigOverride(cfg: Config)(using
      Semigroup[Config]
  ): ComposedConfig[Config, Clazz] =
    config.focus(_.configOverrides).modify(_ :+ (c => c |+| cfg))

  def withOverride[T](
      focus: LazyFocus[Clazz, T],
      value: T
  ): ComposedConfig[Config, Clazz] =
    config.focus(_.valueOverrides).modify(_ :+ (c => focus(c).replace(value)))
}
