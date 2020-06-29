package course.repository
import java.util.UUID
import course.model._
import cats._
import cats.implicits._
import cats.effect._
import java.util.UUID
import scala.collection.mutable.Map
import skunk._
import skunk.implicits._
import skunk.codec.all._

trait CourseRepo[F[_]] {
  def getCourse(id: UUID): F[Option[Course]]
  def getCourses(ids: List[UUID])(implicit F: Applicative[F]): F[List[Course]] =
    ids.traverse(getCourse).map(_.flatten)
  def getCoursesInPage(pageNumber: Int, limitPerPage: Int): F[List[Course]]
  def createCourse(name: String): F[Course]
  def deleteCouse(id: UUID): F[Unit]
  def modifyCourse(id: UUID, course: Course): F[Option[Course]]
}


final case class CourseRepoInMem[F[_]](env: Map[UUID, Course])(implicit
    F: Sync[F]
) extends CourseRepo[F] {

  override def getCourse(id: UUID): F[Option[Course]] =
    F.delay {
      env.get(id)
    }

  override def getCoursesInPage(pageNumber: Int, limitPerPage: Int): F[List[Course]] = {
    val startIndex = pageNumber * limitPerPage
    val ids = env.drop(startIndex).take(limitPerPage).keySet.toList
    getCourses(ids)
  }

  override def deleteCouse(id: UUID): F[Unit] =
    F.delay {
      env.remove(id)
      ()
    }

  override def modifyCourse(id: UUID, course: Course): F[Option[Course]] = {
    env -= id
    env += (id -> course)
    getCourse(id)
  }

  override def createCourse(name: String): F[Course] =
    F.delay {
      val id = UUID.randomUUID()
      val course = Course(id, name)
      env += (id -> course)
      course
    }
}


final case class CourseRepoSkunk[F[_]: Sync](session: Session[F]) extends CourseRepo[F] {

  val courseDecoder: Decoder[Course] = (uuid ~ varchar).map {
    case (i, n) => Course(i, n)
  }

  override def getCourse(id: UUID): F[Option[Course]] = {

    val query: Query[UUID, Course] =
      sql"SELECT ID, NAME FROM COURSE WHERE ID = $uuid"
        .query(courseDecoder)

    session.prepare(query).use(_.option(id))
  }

  def getCoursesInPage(pageNumber: Int, limitPerPage: Int): F[List[Course]] = {
    val query: Query[Void, Course] =
      sql"SELECT ID, NAME FROM COURSE"
        .query(courseDecoder)

    session.execute(query)
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

  override def deleteCouse(id: UUID): F[Unit] = {
    val delete: Command[UUID] =
      sql"DELETE FROM COURSE WHERE ID = $uuid".command
    session.prepare(delete).use(_.execute(id)).map(_ => ())
  }

  override def modifyCourse(id: UUID, course: Course): F[Option[Course]] = {
    val update: Command[Course] =
      sql"UPDATE COURSE SET NAME = $varchar WHERE ID = $uuid".command
        .contramap(c => c.name ~ c.id)
    session.prepare(update).use(_.execute(course))

    getCourse(id)
  }

}
