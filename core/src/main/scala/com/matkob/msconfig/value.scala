package com.matkob.msconfig

import monocle.syntax.all._
import monocle.AppliedTraversal
import cats.kernel.Semigroup
import cats.implicits._

trait CmdParameters {
  def get(name: String): Option[String]
}

trait EnvVariables {
  def get(name: String): Option[String]
}

type LazyFocus[T, V] = T => AppliedTraversal[T, V]

case class OpenValue[Value](value: Value, overrides: Seq[Value => Value] = Seq.empty) {
  def compile: OpenValue[Value] = OpenValue(overrides.foldLeft(value)((acc, func) => func(acc)))

  def withCmdOverride[Replacement](name: String, focus: LazyFocus[Value, Replacement])(using
      c: Conversion[String, Replacement],
      cmd: CmdParameters
  ): OpenValue[Value] =
    cmd.get(name).map(value => withOverride(focus, value)).getOrElse(this)

  def withEnvOverride[Replacement](name: String, focus: LazyFocus[Value, Replacement])(using
      c: Conversion[String, Replacement],
      env: EnvVariables
  ): OpenValue[Value] =
    env.get(name).map(value => withOverride(focus, value)).getOrElse(this)

  def withOverride[Replacement](
      focus: LazyFocus[Value, Replacement],
      replacement: Replacement
  ): OpenValue[Value] =
    OpenValue(value, overrides :+ (c => focus(c).replace(replacement)))
}

given [Value](using Semigroup[Value]): Semigroup[OpenValue[Value]] =
  (a, b) => OpenValue(a.value |+| b.value, a.overrides |+| b.overrides)
