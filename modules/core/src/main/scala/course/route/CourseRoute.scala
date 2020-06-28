package course.route

import org.http4s._
import cats.effect._
import org.http4s.dsl._
import org.http4s._
import course.model._
import java.util.UUID
import sttp.tapir._
import sttp.tapir.json.circe._

trait CourseRoute[F[_]] {
  def route: HttpRoutes[F]
}

object CourseRoute {

  val courseById = endpoint
    .get
    .in(("course" / path[String]("uuid")))
    .errorOut(stringBody)
    .out(jsonBody[Course])

  def dsl[F[_]: Sync] =
    new CourseRoute[F] with Http4sDsl[F] {
      def route =
        HttpRoutes.of[F] {
          case GET -> Root / "course" / id =>
            val course = Course(UUID.randomUUID(), s"Data Structures $id")
            import org.http4s.circe.CirceEntityEncoder._
            Ok(course)
        }
    }

}
