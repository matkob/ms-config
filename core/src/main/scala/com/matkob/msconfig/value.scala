package com.matkob
package msconfig

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

case class OpenValue[Value](product: Value, overrides: Seq[Value => Value] = Seq.empty) {
  def compile: Value = overrides.foldLeft(product)((acc, func) => func(acc))

  def withCliOverride[Replacement](name: String, focus: LazyFocus[Value, Replacement])(using
      c: Conversion[String, Replacement],
      cmd: CmdParameters
  ): OpenValue[Value] = {
    cmd.get(name) match
      case None        => ???
      case Some(value) => withStringOverride(value, focus)
  }

  def withEnvOverride[Replacement](name: String, focus: LazyFocus[Value, Replacement])(using
      c: Conversion[String, Replacement],
      env: EnvVariables
  ): OpenValue[Value] = {
    env.get(name) match
      case None        => ???
      case Some(value) => withStringOverride(value, focus)
  }

  def withStringOverride[Replacement](value: String, focus: LazyFocus[Value, Replacement])(using
      Conversion[String, Replacement]
  ): OpenValue[Value] =
    withOverride(focus, value)

  def withOverride[Replacement](
      focus: LazyFocus[Value, Replacement],
      value: Replacement
  ): OpenValue[Value] =
    OpenValue(product, overrides :+ (c => focus(c).replace(value)))
}

given [Value](using Semigroup[Value]): Semigroup[OpenValue[Value]] =
  (a, b) => OpenValue(a.product |+| b.product, a.overrides |+| b.overrides)
