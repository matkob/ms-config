package com.matkob.msconfig

import com.typesafe.config.{Config, ConfigFactory}
import scala.jdk.CollectionConverters.MapHasAsJava
import fs2.io.file.Path
import com.monovore.decline.Opts

enum ConfigSource:
  case CommandLine(short: String, long: String, help: String)
  case File(path: Path)

object MsConfig {
  def build[T](sources: ConfigSource*) = {
    sources.map {
      case cmd: ConfigSource.CommandLine =>
        Opts.option[T](cmd.long, cmd.help, cmd.long)

      case ConfigSource.File(path) =>
    }
  }
}

case class MsConfig(sources: Seq[Config]) {
  def compile = sources.foldLeft(ConfigFactory.empty()) { case (acc, config) =>
    config.withFallback(acc)
  }
}

object Implicits {
  given map2Config[K, V]: Conversion[Map[K, V], Config] =
    new Conversion[Map[K, V], Config] {
      def apply(x: Map[K, V]): Config =
        ConfigFactory.parseMap(x.map((k, v) => (k.toString, v)).asJava)
    }
}
