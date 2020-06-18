package course.repository
import java.util.UUID
import course.model._
import cats._
import cats.implicits._
import cats.effect._
import java.{util => ju}
import scala.collection.mutable.Map
import skunk._
import skunk.implicits._
import skunk.codec.all._
import scala.NotImplementedError
import scala.collection.mutable.Map

trait CourseRepo[F[_]] {
  def getCourse(id: UUID): F[Option[Course]]
  def getCourses(ids: List[UUID])(implicit F: Applicative[F]): F[List[Course]] =
    ids.traverse(getCourse).map(_.flatten)
  def createCourse(name: String): F[Course]
  def deleteCouse(id: UUID): F[Unit]
  def modifyCourse(id: UUID, course: Course): F[Option[Course]]
}

final case class CourseRepoInMem[F[_]](env: Map[UUID, Course])(
    implicit F: Sync[F]
) extends CourseRepo[F] {

  override def getCourse(id: ju.UUID): F[Option[Course]] = F.delay {
    env.get(id)
  }

  override def deleteCouse(id: ju.UUID): F[Unit] = F.delay {
    env.remove(id)
    ()
  }

  override def modifyCourse(id: ju.UUID, course: Course): F[Option[Course]] = {
    env -= id
    env += (id -> course)
    getCourse(id)
  }

  override def createCourse(name: String): F[Course] = F.delay {
    val id = UUID.randomUUID()
    val course = Course(id, name)
    env += (id -> course)
    course
  }
}

final case class CourseRepoSkunk[F[_]: Sync](session: Session[F])
    extends CourseRepo[F] {

  override def getCourse(id: ju.UUID): F[Option[Course]] = {
    val courseDecoder: Decoder[Course] = (uuid ~ varchar).map {
      case (i, n) => Course(i, n)
    }

    val query: Query[UUID, Course] =
      sql"SELECT ID, NAME FROM COURSE WHERE ID = $uuid"
        .query(courseDecoder)

    session.prepare(query).use(_.option(id))
  }

  override def createCourse(name: String): F[Course] = {
    val insert: Command[Course] =
      sql"INSERT INTO COURSE (ID, NAME) VALUES ($uuid, $varchar)".command
        .contramap(c => c.id ~ c.name)

    val course = Course(UUID.randomUUID(), name)

    for {
      _ <- session.prepare(insert).use(_.execute(course))
    } yield course
  }

  override def deleteCouse(id: ju.UUID): F[Unit] =
    Sync[F].raiseError(new NotImplementedError)

  override def modifyCourse(id: ju.UUID, course: Course): F[Option[Course]] =
    Sync[F].raiseError(new NotImplementedError)

}
