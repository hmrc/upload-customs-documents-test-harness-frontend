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

package repositories

import base.{IntegrationSpecBase, MongoHelpers}
import models.{UploadedFile, UploadedFilesCallback}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.libs.json.{JsResult, JsValue, Json}

import java.time.ZonedDateTime

class UploadedFilesResponseRepoISpec extends IntegrationSpecBase with BeforeAndAfterEach with BeforeAndAfterAll with MongoHelpers {

  lazy val repo: UploadedFilesResponseRepo = app.injector.instanceOf[UploadedFilesResponseRepo]

  override def beforeEach(): Unit = {
    super.beforeEach()
    removeAll(repo)
  }

  val nonce = 12345

  val uploadedFiles: UploadedFilesCallback = UploadedFilesCallback(
    nonce = nonce,
    uploadedFiles = Seq()
  )
  val jsonInRepo: JsValue = Json.obj(fields =
    "nonce" -> nonce,
    "uploadedFiles" -> Json.arr()
  )

  "repository domainFormatImplicit reads" must {

    "read in json as per format of mongo reads" in {
      val fileUpload: JsResult[UploadedFilesCallback] = Json.fromJson[UploadedFilesCallback](jsonInRepo)(repo.domainFormat)
      fileUpload.get mustBe uploadedFiles
    }
  }

  "repository domainFormatImplicit writes" must {

    "write json as per format of mongo writes" in {
      val fileUploadJson: JsValue = Json.toJson[UploadedFilesCallback](uploadedFiles)(repo.domainFormat)
      fileUploadJson mustBe jsonInRepo
    }
  }

  ".updateRecord" when {

    "no record exists" must {

      "upsert the first" in {

        count(repo) mustBe 0
        await(repo.updateRecord(uploadedFiles))
        count(repo) mustBe 1

        await(repo.getRecord(nonce)) mustBe Some(uploadedFiles)
      }
    }

    "a record exists" must {

      "update the existing record" in {

        count(repo) mustBe 0
        await(repo.updateRecord(uploadedFiles))
        count(repo) mustBe 1

        val updatedResponse = uploadedFiles.copy(uploadedFiles = Seq(
          UploadedFile(
            upscanReference = "upscanRef",
            downloadUrl = "download",
            uploadTimestamp = ZonedDateTime.now(),
            checksum = "checksum",
            fileName = "fileName",
            fileMimeType = "mimeType",
            fileSize = Some(100)
          )
        ))

        await(repo.updateRecord(updatedResponse))
        count(repo) mustBe 1

        await(repo.getRecord(nonce)) mustBe Some(updatedResponse)
      }
    }
  }

  ".getRecordByUpscanReference" must {

    "return the found document when a nonce can be found" in {

      count(repo) mustBe 0
      await(repo.updateRecord(uploadedFiles))
      count(repo) mustBe 1

      await(repo.getRecord(nonce)) mustBe Some(uploadedFiles)
    }

    "return None when a nonce can NOT be found" in {

      count(repo) mustBe 0
      await(repo.updateRecord(uploadedFiles))
      count(repo) mustBe 1

      await(repo.getRecord(5)) mustBe None
    }
  }
}
