/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package models

import play.api.libs.json.{Format, JsObject, Json}

case class UploadedFilesCallback(nonce: Int, uploadedFiles: Seq[UploadedFile], cargo: Option[JsObject] = None)

object UploadedFilesCallback {
  implicit val format: Format[UploadedFilesCallback] = Json.format[UploadedFilesCallback]
}
