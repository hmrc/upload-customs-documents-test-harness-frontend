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

import base.GuicySpec
import config.AppConfig
import connectors.httpParsers.UploadCustomsDocumentsInitializationHttpParser.NoLocationHeaderReturned
import connectors.mocks.MockUploadDocumentsConnector
import forms.UploadCustomsDocumentInitialisationFormProvider
import models.InitialisationModel
import play.api.Configuration
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import views.html.InitialisationPage

import scala.concurrent.Future

class InitialisationControllerSpec extends GuicySpec with MockUploadDocumentsConnector {

  lazy val view = app.injector.instanceOf[InitialisationPage]
  lazy val form = app.injector.instanceOf[UploadCustomsDocumentInitialisationFormProvider]

  object TestController extends InitialisationController(
    mcc,
    view,
    form,
    mockUploadCustomsDocumentsConnector
  )

  "calling .intialiseParams" should {

    "return 200" in {
      val result = TestController.intialiseParams(fakeRequest)
      status(result) mustBe Status.OK
    }

    "return HTML" in {
      val result = TestController.intialiseParams(fakeRequest)
      contentType(result) mustBe Some("text/html")
      charset(result)     mustBe Some("utf-8")
    }
  }

  "calling .postInitialisation" when {

    "form does not contain valid JSON" must {

      "return 400" in {
        val result = TestController.postInitialisation(fakeRequest.withFormUrlEncodedBody("json" -> "", "userAgent" -> "foo"))
        status(result) mustBe Status.BAD_REQUEST
      }
    }

    "form does not contain userAgent" must {

      "return 400" in {
        val result = TestController.postInitialisation(fakeRequest.withFormUrlEncodedBody("json" -> "{}"))
        status(result) mustBe Status.BAD_REQUEST
      }
    }

    "form contains valid JSON" when {

      "response from connector is Right(redirectUrl)" when {

        "the external host url is an internal url (locally)" must {
          "return 303 redirecting to internal url" in {

            mockInitialise(InitialisationModel(Json.obj(), "foo", "internalUrl"))(Future(Right("/foo")))

            val result = TestController.postInitialisation(fakeRequest.withFormUrlEncodedBody("json" -> "{}", "userAgent" -> "foo", "url" -> "internalUrl"))

            status(result) mustBe Status.SEE_OTHER
            redirectLocation(result) mustBe Some("internalUrl" + "/foo")
          }
        }
        "the external host url is not internal url (non-local)" must {

          "return 303 redirecting to external host url" in {

            mockInitialise(InitialisationModel(Json.obj(), "foo", "bar"))(Future(Right("/foo")))

            lazy val appConfig: AppConfig = new AppConfig(inject[Configuration], inject[ServicesConfig]) {
              override val host: String = "baz"
              override val hostDNS: String = "localhost"
            }

            val testController =
              new InitialisationController(mcc, view, form, mockUploadCustomsDocumentsConnector)(ec, appConfig)
            val result = testController.postInitialisation(fakeRequest.withFormUrlEncodedBody("json" -> "{}", "userAgent" -> "foo", "url" -> "bar"))

            status(result) mustBe Status.SEE_OTHER
            redirectLocation(result) mustBe Some("baz" + "/foo")
          }
        }
      }

      "response from connector is Left(_)" must {

        "return ISE" in {

          mockInitialise(InitialisationModel(Json.obj(), "foo", "bar"))(Future(Left(NoLocationHeaderReturned)))

          val result = TestController.postInitialisation(fakeRequest.withFormUrlEncodedBody("json" -> "{}", "userAgent" -> "foo", "url" -> "bar"))

          status(result) mustBe Status.INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
