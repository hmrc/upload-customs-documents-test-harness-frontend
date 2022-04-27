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

package connectors

import base.GuicySpec
import connectors.httpParsers.UploadCustomsDocumentsInitializationHttpParser.NoLocationHeaderReturned
import connectors.mocks.MockHttp
import models.InitialisationModel
import play.api.http.HeaderNames
import play.api.libs.json.Json

class UploadCustomsDocumentsConnectorSpec extends GuicySpec with MockHttp {

  val locationHeaderUrl = "/foo"
  val initModel = InitialisationModel(Json.obj("foo" -> "bar").toString, "foo-bar-agent", appConfig.uploadCustomsDocumentsDNS)

  object TestUploadCustomsDocumentsConnector extends UploadCustomsDocumentsConnector(mockHttp)

  "UploadCustomsDocumentsConnector" must {

    "For the .initialise() method" must {

      "for a successful response" must {

        "return a Right(locationHeaderUrl)" in {

          setupMockHttpPost(
            url = appConfig.uploadCustomsDocumentsDNS + "/internal/initialize",
            model = Json.parse(initModel.json),
            headers = Seq(HeaderNames.USER_AGENT -> initModel.userAgent)
          )(Right(locationHeaderUrl))

          val expectedResult = Right(locationHeaderUrl)
          val actualResult = TestUploadCustomsDocumentsConnector.initialize(initModel)(hc, ec)

          await(actualResult) mustBe expectedResult
        }
      }

      "for an error response" must {

        "return a Left(Invalid)" in {

          setupMockHttpPost(
            url = appConfig.uploadCustomsDocumentsDNS + "/internal/initialize",
            model = Json.parse(initModel.json),
            headers = Seq(HeaderNames.USER_AGENT -> initModel.userAgent)
          )(Left(NoLocationHeaderReturned))

          val expectedResult = Left(NoLocationHeaderReturned)
          val actualResult = TestUploadCustomsDocumentsConnector.initialize(initModel)(hc, ec)

          await(actualResult) mustBe expectedResult
        }
      }
    }
  }
}
