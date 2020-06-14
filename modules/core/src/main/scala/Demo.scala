import cats.effect._
import cats.implicits._
import cats.effect.implicits._
import skunk._
import skunk.implicits._
import skunk.codec.all._
import natchez.Trace.Implicits.noop
import java.util.UUID
import course.model._

object Hello extends IOApp {

  val session: Resource[IO, Session[IO]] =
    Session.single(
      host = "localhost",
      port = 5432,
      user = "postgres",
      database = "course-store",
      password = Some("postgres")
    )

  def run(args: List[String]): IO[ExitCode] = {

    // val insert: Command[Course] = sql"INSERT INTO COURSE (ID, NAME) VALUES ($uuid, $varchar)"
    //     .command
    //     .contramap(c => c.id ~ c.name)

    // val course = Course(UUID.randomUUID(), "Scala in 20 minutes")

    // session.use { s =>
    //   s.prepare(insert).use { s2 =>
    //     for {
    //       _ <- s2.execute(course)
    //     } yield ExitCode.Success
    //   }
    // }

    val query: Query[Void, Course] = sql"SELECT ID, NAME FROM COURSE"
      .query(uuid ~ varchar)
      .map { case id ~ name => Course(id, name) }

    session.use { s =>
      for {
        courses <- s.execute(query)
        _ <- IO(println(courses))
      } yield ExitCode.Success
    }

  }

}
