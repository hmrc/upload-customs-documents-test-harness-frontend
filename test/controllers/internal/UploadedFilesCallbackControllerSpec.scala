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

import base.GuicySpec
import models.{UploadedFile, UploadedFilesCallback}
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadedFilesResponseRepo

import java.time.{Instant, ZoneId, ZonedDateTime}

class UploadedFilesCallbackControllerSpec extends GuicySpec with BeforeAndAfterEach {

  val repo = app.injector.instanceOf[UploadedFilesResponseRepo]

  object TestController extends UploadedFilesCallbackController(mcc, repo)

  val nonce = 12345

  val callbackPayload: UploadedFilesCallback =
    UploadedFilesCallback(
      nonce = nonce,
      uploadedFiles = Seq(uploadDocument()),
      cargo = None
    )

  def uploadDocument(id: String = "pdf"): UploadedFile = UploadedFile(
    upscanReference = s"upscan-reference-$id",
    fileName = s"file-name-$id",
    downloadUrl = s"download-url-$id",
    uploadTimestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.of("Europe/London")),
    checksum = "A" * 64,
    fileMimeType = s"application/$id",
    fileSize = Some(12345)
  )

  "POST /" should {

    "return 204 if callback accepted AND update mongo" in {
      val result = TestController.post()(FakeRequest("Post", "/").withBody(Json.toJson(callbackPayload)))
      status(result) mustBe 204

      await(repo.getRecord(nonce)) mustBe Some(callbackPayload)
    }

    "return 400 if callback rejected because of invalid request" in {
      val result = TestController.post()(FakeRequest("Post", "/").withBody(Json.parse("""{"foo":"bar"}""")))
      status(result) mustBe 400
    }
  }
}
