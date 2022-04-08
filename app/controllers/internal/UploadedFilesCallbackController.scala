/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package controllers.internal

import config.AppConfig
import models.UploadedFilesCallback
import play.api.libs.json.JsValue
import play.api.mvc.{Action, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UploadedFilesCallbackController @Inject() (mcc: MessagesControllerComponents)(implicit
  val ec: ExecutionContext,
  val appConfig: AppConfig
) extends FrontendController(mcc) {

  val post: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[UploadedFilesCallback] { uploadedFilesCallback =>
      Future.successful(NoContent)
    }
  }
}
