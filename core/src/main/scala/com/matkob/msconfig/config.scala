package com.matkob.msconfig

import cats.kernel.Semigroup
import cats.implicits._

import com.matkob.msconfig.OpenValue
import cats.Applicative

case class OpenConfig[Config](config: Config, overrides: Seq[Config => Config] = Seq.empty) {

  def compile: OpenConfig[Config] =
    OpenConfig(overrides.foldLeft(config)((acc, func) => func(acc)))

  def value[Product](using c: Conversion[Config, Product]): OpenValue[Product] =
    OpenValue(compile.config)

  def valueF[F[_]: Applicative, Product](using c: Conversion[Config, F[Product]]): F[OpenValue[Product]] =
    c(compile.config).map(OpenValue(_))

  def withConfigOverride(cfg: Config)(using Semigroup[Config]): OpenConfig[Config] =
    OpenConfig(config, overrides :+ (c => c |+| cfg))

}

given [Config](using Semigroup[Config]): Semigroup[OpenConfig[Config]] =
  (a, b) => OpenConfig(a.config |+| b.config, a.overrides |+| b.overrides)
