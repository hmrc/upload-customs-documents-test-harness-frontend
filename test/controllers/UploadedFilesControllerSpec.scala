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
import connectors.mocks.MockUploadDocumentsConnector
import play.api.http.Status
import play.api.test.Helpers._
import repositories.UploadedFilesResponseRepo
import views.html.UploadedFilesPage

class UploadedFilesControllerSpec extends GuicySpec with MockUploadDocumentsConnector {

  lazy val view = app.injector.instanceOf[UploadedFilesPage]
  lazy val repo = app.injector.instanceOf[UploadedFilesResponseRepo]

  object TestController extends UploadedFilesController(mcc, view, repo)

  val nonce = 12345

  "calling .listFiles(nonce)" should {

    "return 200" in {
      val result = TestController.listFiles(nonce)(fakeRequest)
      status(result) mustBe Status.OK
    }

    "return HTML" in {
      val result = TestController.listFiles(nonce)(fakeRequest)
      contentType(result) mustBe Some("text/html")
      charset(result)     mustBe Some("utf-8")
    }
  }
}
