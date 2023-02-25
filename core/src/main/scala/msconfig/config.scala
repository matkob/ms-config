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

class ComposedConfig[Config, Clazz](
    config: Config,
    // TODO: allow for mixed config, value override order, zip with index or different impl
    configOverrides: List[Config => Config] = Nil,
    valueOverrides: List[Clazz => Clazz] = Nil
) {

  def compile(decoder: Config => Clazz): Clazz = {
    val cfg = configOverrides.foldLeft(config)((acc, func) => func(acc))
    valueOverrides.foldLeft(decoder(cfg))((acc, func) => func(acc))
  }

  def withCliOverride[T](
      name: String,
      focus: LazyFocus[Clazz, T]
  )(using
      c: Conversion[String, T],
      cmd: CmdParameters
  ): ComposedConfig[Config, Clazz] = {
    cmd.get(name) match
      case None        => ???
      case Some(value) => withStringOverride(value, focus)
  }

  def withEnvOverride[T](
      name: String,
      focus: LazyFocus[Clazz, T]
  )(using
      c: Conversion[String, T],
      env: EnvVariables
  ): ComposedConfig[Config, Clazz] = {
    env.get(name) match
      case None        => ???
      case Some(value) => withStringOverride(value, focus)
  }

  def withStringOverride[T](
      value: String,
      focus: LazyFocus[Clazz, T]
  )(using Conversion[String, T]): ComposedConfig[Config, Clazz] =
    withOverride(focus, value)

  def withConfigOverride(cfg: Config)(using
      Semigroup[Config]
  ): ComposedConfig[Config, Clazz] =
    ComposedConfig(config, configOverrides :+ (c => c |+| cfg), valueOverrides)

  def withOverride[T](
      focus: LazyFocus[Clazz, T],
      value: T
  ): ComposedConfig[Config, Clazz] =
    ComposedConfig(
      config,
      configOverrides,
      valueOverrides :+ (c => focus(c).replace(value))
    )
}
