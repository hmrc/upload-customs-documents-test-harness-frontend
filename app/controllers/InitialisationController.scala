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
import connectors.UploadCustomsDocumentsConnector
import forms.UploadCustomsDocumentInitialisationFormProvider
import models.InitialisationModel
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.HtmlFormat
import repositories.UploadedFilesResponseRepo
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.InitialisationPage

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InitialisationController @Inject() (
  mcc: MessagesControllerComponents,
  view: InitialisationPage,
  intialisationForm: UploadCustomsDocumentInitialisationFormProvider,
  uploadCustomsDocumentsConnector: UploadCustomsDocumentsConnector,
  uploadedFilesResponseRepo: UploadedFilesResponseRepo
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) {

  private def renderView(form: Form[_])(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable =
    view(controllers.routes.InitialisationController.postInitialisation, form)

  val intialiseParams: Action[AnyContent] = Action.async { implicit request =>
    uploadedFilesResponseRepo.getRecord(12345).map { maybePreviousFiles =>
      Ok(renderView(intialisationForm().fill(InitialisationModel.defaultConfig(maybePreviousFiles))))
    }
  }

  val postInitialisation: Action[AnyContent] = Action.async { implicit request =>
    intialisationForm()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(renderView(formWithErrors))),
        initialisationModel =>
          uploadCustomsDocumentsConnector.initialize(initialisationModel).map {
            case Left(_)         => InternalServerError
            case Right(redirect) =>
              val host =
                if(appConfig.hostDNS == appConfig.host) initialisationModel.url else appConfig.host
              Redirect(host + redirect)
          }
      )
  }
}
