package model
import java.util.UUID
import io.circe._, io.circe.parser._, io.circe.syntax._, io.circe.generic.semiauto._

final case class Course(id: UUID, name: String)
object Course {
  implicit val encoder = deriveEncoder[Course]
  implicit val decoder = deriveDecoder[Course]
}
