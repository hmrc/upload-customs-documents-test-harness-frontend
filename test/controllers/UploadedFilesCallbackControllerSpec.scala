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

import akka.stream.Materializer
import controllers.internal.UploadedFilesCallbackController
import models.{UploadedFile, UploadedFilesCallback}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

import java.time.{Instant, ZoneId, ZonedDateTime}

class UploadedFilesCallbackControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure("metrics.jvm" -> false, "metrics.enabled" -> false)
      .build()
  implicit val materializer: Materializer = app.materializer

  private val controller = app.injector.instanceOf[UploadedFilesCallbackController]

  val callbackPayload: UploadedFilesCallback =
    UploadedFilesCallback(
      nonce = 12345,
      uploadedFiles = Seq(uploadDocument()),
      cargo = None
    )

  def uploadDocument(id: String = "foo") = UploadedFile(
    upscanReference = s"upscan-reference-$id",
    fileName = s"file-name-$id",
    downloadUrl = s"download-url-$id",
    uploadTimestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.of("Europe/London")),
    checksum = "A" * 64,
    fileMimeType = s"application/$id",
    fileSize = Some(12345)
  )
  "POST /" should {
    "return 204 if callback accepted" in {
      val result = controller.post(FakeRequest().withJsonBody(Json.toJson(callbackPayload)))
      status(result) shouldBe 204
    }

    "return 400 if callback rejected because of invalid request" in {
      val result = controller.post(FakeRequest().withJsonBody(Json.parse("""{"foo":"bar"}""")))
      status(result) shouldBe 400
    }
  }
}
