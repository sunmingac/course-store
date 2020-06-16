package course.model
import java.util.UUID
import io.circe.generic.semiauto._

final case class Course(id: UUID, name: String)
object Course {
  implicit val encoder = deriveEncoder[Course]
  implicit val decoder = deriveDecoder[Course]
}
