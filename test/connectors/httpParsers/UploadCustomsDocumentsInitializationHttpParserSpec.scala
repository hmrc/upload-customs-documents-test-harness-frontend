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

package connectors.httpParsers

import base.SpecBase
import connectors.httpParsers.UploadCustomsDocumentsInitializationHttpParser.NoLocationHeaderReturned
import connectors.httpParsers.UploadCustomsDocumentsInitializationHttpParser.UnexpectedFailure
import connectors.httpParsers.UploadCustomsDocumentsInitializationHttpParser.UploadCustomsDocumentsInitializationReads
import play.api.http.HeaderNames
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse

class UploadCustomsDocumentsInitializationHttpParserSpec extends SpecBase {

  "UploadCustomsDocumentsInitializationHttpParser.UploadCustomsDocumentsInitializationReads" when {

    "given an (201) CREATED" when {

      "the Location header is returned" must {

        "return url" in {

          val expectedResult = Right("/foo")
          val actualResult   = UploadCustomsDocumentsInitializationReads.read(
            "",
            "",
            HttpResponse(Status.CREATED, json = Json.obj(), Map(HeaderNames.LOCATION -> Seq("/foo")))
          )

          actualResult mustBe expectedResult
        }
      }

      "the location header is NOT returned" must {

        "return a NoLocationHeaderReturned with the value false" in {

          val expectedResult = Left(NoLocationHeaderReturned)
          val actualResult   = UploadCustomsDocumentsInitializationReads.read(
            "",
            "",
            HttpResponse(Status.CREATED, json = Json.obj(), Map())
          )

          actualResult mustBe expectedResult
        }
      }
    }

    "given any other status" must {

      "return a Left(UnexpectedFailure)" in {

        val expectedResult = Left(UnexpectedFailure(status = Status.INTERNAL_SERVER_ERROR))
        val actualResult   =
          UploadCustomsDocumentsInitializationReads.read("", "", HttpResponse(Status.INTERNAL_SERVER_ERROR, ""))

        actualResult mustBe expectedResult
      }
    }
  }
}
