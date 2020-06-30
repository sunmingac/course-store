package course

import cats.data._
import cats.implicits._
import cats.effect._
import org.http4s.server.blaze._
import org.http4s._
import org.http4s.implicits._
import route._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext
import course.repository._

object Main extends IOApp {

  def run(args: List[String]) = {

    // val courseRepo = CourseRepo.courseRepositoryInPostgres[IO]
    val courseRepo = CourseRepo.courseRepositoryInMemory[IO]

    val httpApp: HttpApp[IO] = NonEmptyChain(
      CourseRoute.dsl[IO](courseRepo).routes
    ).reduceLeft(_ <+> _).orNotFound

    val executionContext = implicitly[ExecutionContext]
    val server = BlazeServerBuilder[IO](executionContext)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .resource
      .use(_ => IO.never)
    // .as(ExitCode.Success) // put this line back when deploy to cloud

    // remove the for comprehension when deploy to cloud
    for {
      fiber <- server.start
      _ <- IO(println(Console.MAGENTA + "Press ENTER to shutdown server" + Console.RESET))
      _ <- IO(scala.io.StdIn.readLine())
      _ <- fiber.cancel
    } yield ExitCode.Success
  }

}
