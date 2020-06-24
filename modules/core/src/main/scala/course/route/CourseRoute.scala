package course.route

import org.http4s._
import cats.effect._
import org.http4s.dsl._
import org.http4s._
import course.model._
import java.util.UUID

trait CourseRoute[F[_]] {
  def route: HttpRoutes[F]
}

object CourseRoute {

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
