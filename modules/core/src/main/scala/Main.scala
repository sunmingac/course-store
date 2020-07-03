package course

import cats.Monad
import cats.data._
import cats.implicits._
import cats.effect._
import org.http4s.server.blaze._
import org.http4s.implicits._
import route._
import scala.util.chaining._
import scala.concurrent.ExecutionContext.global
import course.repository._

object Main extends IOApp {

  def run(args: List[String]) =
    for {
      config    <- Config.appConfig
      courseRepo = CourseRepo.courseRepositoryInMemory[IO] // In memory database
      // courseRepo = CourseRepo.courseRepositoryInPostgres[IO](config.postgres) //Postgres database
      httpApp    = NonEmptyChain(
                     CourseRoute.dsl[IO](courseRepo).routes
                   ).reduceLeft(_ <+> _).orNotFound

      _         <- BlazeServerBuilder[IO](global)
                     .bindHttp(config.port, "0.0.0.0")
                     .withHttpApp(httpApp)
                     .serve
                     .compile
                     .drain
                     .pipe(devServer[IO]) // use "devServer" for development, delete this line in production
    } yield ExitCode.Success

  def devServer[F[_]: Concurrent](server: F[Unit]): F[Unit] =
    for {
      fiber <- Concurrent[F].start(server)
      _     <- Monad[F].pure(println(Console.MAGENTA + "Press ENTER to shutdown server" + Console.RESET))
      _     <- Monad[F].pure(scala.io.StdIn.readLine())
      _     <- fiber.cancel
    } yield ()
}
