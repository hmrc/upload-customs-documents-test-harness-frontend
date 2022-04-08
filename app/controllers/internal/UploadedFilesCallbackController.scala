/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.internal

import config.AppConfig
import models.UploadedFilesCallback
import play.api.libs.json.JsValue
import play.api.mvc.{Action, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UploadedFilesCallbackController @Inject() (mcc: MessagesControllerComponents)(implicit
  val ec: ExecutionContext,
  val appConfig: AppConfig
) extends FrontendController(mcc) {

  // {"nonce":12345,"uploadedFiles":[{"upscanReference":"upscan-reference-pdf","downloadUrl":"download-url-pdf","uploadTimestamp":"1970-01-01T01:00:00+01:00[Europe/London]","checksum":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA","fileName":"file-name-pdf","fileMimeType":"application/pdf","fileSize":12345}]}
  val post: Action[JsValue] = Action.async(parse.tolerantJson) { implicit request =>
    withJsonBody[UploadedFilesCallback] { uploadedFilesCallback =>
      Future.successful(NoContent)
    }
  }
}
