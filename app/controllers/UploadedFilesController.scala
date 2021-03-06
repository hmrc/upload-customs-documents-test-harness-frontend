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

package controllers

import config.AppConfig
import models.UploadedFile
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.UploadedFilesResponseRepo
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggerUtil
import views.html.UploadedFilesPage

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UploadedFilesController @Inject()(mcc: MessagesControllerComponents,
                                        view: UploadedFilesPage,
                                        uploadedFilesRepo: UploadedFilesResponseRepo)
                                       (implicit ec: ExecutionContext, appConfig: AppConfig) extends FrontendController(mcc) with LoggerUtil {

  def listFiles(nonce: Int): Action[AnyContent] = Action.async { implicit request =>
    uploadedFilesRepo.getRecord(nonce).map { oUploadedFiles =>
      val files = oUploadedFiles.fold[Seq[UploadedFile]](Seq())(_.uploadedFiles)
      logger.info(s"[listFiles] Mongo Record: $oUploadedFiles")
      Ok(view(files, oUploadedFiles.isDefined))
    }
  }
}
