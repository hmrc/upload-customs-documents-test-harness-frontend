package models

import play.api.libs.json.{Format, JsObject, Json}

import java.time.ZonedDateTime

final case class UploadedFile(
  upscanReference: String,
  downloadUrl: String,
  uploadTimestamp: ZonedDateTime,
  checksum: String,
  fileName: String,
  fileMimeType: String,
  fileSize: Option[Long],
  cargo: Option[JsObject] = None,
  description: Option[String] = None
)

object UploadedFile {
  implicit val formats: Format[UploadedFile] = Json.format[UploadedFile]
}
