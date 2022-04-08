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
import connectors.httpParsers.UploadCustomsDocumentsInitializationHttpParser.NoLocationHeaderReturned
import connectors.mocks.MockUploadDocumentsConnector
import forms.UploadCustomsDocumentInitialisationFormProvider
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
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
        val result = TestController.postInitialisation(fakeRequest.withFormUrlEncodedBody("json" -> ""))
        status(result) mustBe Status.BAD_REQUEST
      }
    }

    "form contains valid JSON" when {

      "response from connector is Right(redirectUrl)" must {

        "return 303" in {

          mockInitialise(Json.obj())(Future(Right("/foo")))

          val result = TestController.postInitialisation(fakeRequest.withFormUrlEncodedBody("json" -> "{}"))

          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result) mustBe Some(appConfig.uploadCustomsDocumentsUrl + "/foo")
        }
      }

      "response from connector is Left(_)" must {

        "return ISE" in {

          mockInitialise(Json.obj())(Future(Left(NoLocationHeaderReturned)))

          val result = TestController.postInitialisation(fakeRequest.withFormUrlEncodedBody("json" -> "{}"))

          status(result) mustBe Status.INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
