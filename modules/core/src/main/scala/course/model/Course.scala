package course.model
import java.util.UUID
import io.circe.generic.semiauto._

final case class Course(id: UUID, name: String)
object Course {
  implicit val courseCodec = deriveCodec[Course]
}

final case class CourseWithoutUUID(name: String)
object CourseWithoutUUID {
  implicit val courseWithoutUUIDCodec = deriveCodec[CourseWithoutUUID]
}
