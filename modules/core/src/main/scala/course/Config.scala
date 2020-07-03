package course

import scala.util.chaining._
import cats.effect.IO
import cats.implicits._
import pureconfig.ConfigSource
import pureconfig.generic.auto._

case class AppConfig(port: Int, postgres: Postgres)

case class Postgres(
    host: String,
    port: Int,
    database: String,
    user: String,
    password: String
)

object Config {
  def appConfig: IO[AppConfig] =
    ConfigSource.default
      .load[AppConfig]
      .leftMap(cf => new Exception(cf.head.description))
      .pipe(IO.fromEither[AppConfig])
}
