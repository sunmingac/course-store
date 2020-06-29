package course.route

import cats.data._
import cats.effect._
import cats.implicits._
import org.http4s.dsl._
import org.http4s._
import course.model._
import java.util.UUID
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s._
import sttp.tapir.server.http4s.Http4sServerOptions._
import sttp.tapir.openapi.OpenAPI
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.swagger.http4s.SwaggerHttp4s

trait CourseRoute[F[_]] {
  def routes: HttpRoutes[F]
}

object CourseRoute {
  import CourseEndpoint._

  def dsl[F[_]: Sync](implicit C: ContextShift[F]) =
    new CourseRoute[F] with Http4sDsl[F] {

      def routes =
        NonEmptyChain(
          routeSwagger,
          routeGetCourse,
          routeGetCourseInPage,
          routeCreateCourse,
          routeModifyCourse,
          routeDeleteCourse
        ).reduceLeft(_ <+> _)

      private def routeSwagger = {
        val docs: OpenAPI = allEndpoints.toOpenAPI("Course Store", "1.0")
        new SwaggerHttp4s(docs.toYaml).routes[F]
      }

      private def routeGetCourse =
        courseById.toRoutes(uuid =>
          Sync[F].delay {
            val course = Course(UUID.randomUUID(), s"Data Structures $uuid")
            course.asRight[Unit]
          }
        )

      private def routeGetCourseInPage =
        courseInPage.toRoutes {
          case (page, limit) =>
            Sync[F].delay {
              val c1 = Course(UUID.randomUUID(), s"Data Structures")
              val c2 = Course(UUID.randomUUID(), s"Mathmatics $page $limit")
              List(c1, c2).asRight[Unit]
            }
        }

      private def routeCreateCourse =
        createCourse.toRoutes(course =>
          Sync[F].delay {
            Course(UUID.randomUUID(), course.name).asRight[Unit]
          }
        )

      private def routeModifyCourse =
        modifyCourse.toRoutes {
          case (uuid, course) =>
            Sync[F].delay {
              Course(uuid, course.name).asRight[Unit]
            }
        }

      private def routeDeleteCourse =
        deleteCourse.toRoutes { _ =>
          Sync[F].pure(().asRight[Unit])
        }
    }
}

object CourseEndpoint {

  def allEndpoints =
    List(
      courseById,
      courseInPage,
      createCourse,
      modifyCourse,
      deleteCourse
    )

  val courseById: Endpoint[UUID, Unit, Course, Nothing] =
    endpoint.get
      .description("Find Course by UUID")
      .in(("course" / path[UUID]("uuid")))
      .out(jsonBody[Course])

  val courseInPage: Endpoint[(Int, Int), Unit, List[Course], Nothing] =
    endpoint.get
      .description("Find Courses")
      .in(("course"))
      .in(
        query[Int]("page")
          .description("Page number")
          .and(query[Int]("limit").description("Number of records per page"))
      )
      .out(jsonBody[List[Course]])

  val createCourse: Endpoint[CourseWithoutUUID, Unit, Course, Nothing] =
    endpoint.post
      .description("Create Course")
      .in(("course"))
      .in(jsonBody[CourseWithoutUUID].description("Course Name"))
      .out(jsonBody[Course])

  val modifyCourse: Endpoint[(UUID, CourseWithoutUUID), Unit, Course, Nothing] =
    endpoint.put
      .description("Modify Course")
      .in(("course" / path[UUID]("uuid")))
      .in(jsonBody[CourseWithoutUUID].description("Course Name"))
      .out(jsonBody[Course])

  val deleteCourse: Endpoint[UUID, Unit, Unit, Nothing] =
    endpoint.delete
      .description("Delete Course")
      .in(("course" / path[UUID]("uuid")))
}
