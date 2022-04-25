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

import models.UploadedFilesCallback
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{FindOneAndReplaceOptions, IndexModel, IndexOptions, ReturnDocument}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import utils.LoggerUtil

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UploadedFilesResponseRepo @Inject()(mongo: MongoComponent)(implicit ec: ExecutionContext) extends
  PlayMongoRepository[UploadedFilesCallback](
    collectionName = "file-upload-callback",
    mongoComponent = mongo,
    domainFormat = UploadedFilesCallback.format,
    indexes = Seq(
      IndexModel(
        keys = ascending("nonce"),
        indexOptions = IndexOptions()
          .name("nonce-Unique-Index")
          .unique(true)
          .sparse(false)
      )
    )
  ) with LoggerUtil {

  def updateRecord(callbackData: UploadedFilesCallback)(implicit ec: ExecutionContext): Future[Option[UploadedFilesCallback]] = {
    val selector = equal("nonce", callbackData.nonce)
    collection
      .findOneAndReplace(
        filter = selector,
        replacement = callbackData,
        options = FindOneAndReplaceOptions()
          .returnDocument(ReturnDocument.AFTER)
          .upsert(true)
      ).toFutureOption()

  }

  def getRecord(nonce: Int): Future[Option[UploadedFilesCallback]] = {
    val selector = equal("nonce", nonce)
    collection
      .find(selector)
      .headOption()
  }
}
