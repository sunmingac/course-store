import cats.effect._
import cats.implicits._
import cats.effect.implicits._
import skunk._
import skunk.implicits._
import skunk.codec.all._
import natchez.Trace.Implicits.noop
import java.util.UUID
import model.Course
import repository._

object Server extends IOApp {

  val session: Resource[IO, Session[IO]] =
    Session.single(
      host     = "localhost",
      port     = 5432,
      user     = "postgres",
      database = "course-store",
      password = Some("postgres"),
    )

  def run(args: List[String]): IO[ExitCode] = {
    
    session.use { s =>
      for {
        _ <- IO()
        repo = CourseRepoSkunk[IO](s)
        c1 <- repo.createCourse("Scala for dummies")
        _ = println(s"Course created: $c1")
        c2 <- repo.getCourse(c1.id)
        _ = println(s"Course found: $c2")
      } yield ExitCode.Success
    }
  }
}