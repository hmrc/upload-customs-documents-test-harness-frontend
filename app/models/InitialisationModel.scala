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

package models

import config.AppConfig
import play.api.libs.json.{JsObject, Json}

case class InitialisationModel(json: String, userAgent: String, url: String)

object InitialisationModel {

  private def minimumJson(maybePreviousFiles: Option[UploadedFilesCallback])(implicit appConfig: AppConfig): JsObject = {

    val previousFiles = maybePreviousFiles.fold(Json.obj())(files => Json.obj("existingFiles" -> files.uploadedFiles))

    Json.obj(
      "config" -> Json.obj(
        "nonce" -> 12345,
        "continueUrl" -> appConfig.continueUrl(12345),
        "backlinkUrl" -> appConfig.backLinkUrl,
        "callbackUrl" -> appConfig.callbackDNSRoute
      )
    ).deepMerge(previousFiles)
  }

  def defaultConfig(maybePreviousFiles: Option[UploadedFilesCallback])(implicit appConfig: AppConfig): InitialisationModel =
    InitialisationModel(Json.prettyPrint(minimumJson(maybePreviousFiles)), appConfig.defaultUserAgent, appConfig.uploadCustomsDocumentsDNS)
}
