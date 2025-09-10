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
import play.api.libs.json.JsObject
import play.api.libs.json.Json

case class InitialisationModel(json: String, userAgent: String, url: String)

object InitialisationModel {

  private def minimumJson(
    maybePreviousFiles: Option[UploadedFilesCallback]
  )(implicit appConfig: AppConfig): JsObject = {

    val previousFiles = maybePreviousFiles.fold(Json.obj())(files => Json.obj("existingFiles" -> files.uploadedFiles))

    Json
      .obj(
        "config" -> Json.obj(
          "nonce"                     -> 12345,
          "continueUrl"               -> appConfig.continueUrl(12345),
          "continueAfterYesAnswerUrl" -> appConfig.continueUrl(12345),
          "continueWhenFullUrl"       -> appConfig.continueUrl(12345),
          "backlinkUrl"               -> appConfig.backLinkUrl,
          "callbackUrl"               -> appConfig.callbackDNSRoute,
          "minimumNumberOfFiles"      -> 1,
          "maximumNumberOfFiles"      -> 100,
          "initialNumberOfEmptyRows"  -> 1,
          "maximumFileSizeBytes"      -> 9000000,
          "allowedContentTypes"       -> "application/pdf,image/jpeg,image/png,text/csv,text/plain,application/vnd.ms-outlook,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.oasis.opendocument.text,application/vnd.oasis.opendocument.spreadsheet",
          "allowedFileExtensions"     -> ".pdf,.png,.jpg,.jpeg,.csv,.txt,.msg,.pst,.ost,.eml,.doc,.docx,.xls,.xlsx,.ods,.odt",
          "cargo"                     -> "FooFile",
          "newFileDescription"        -> "Foo file",
          "content"                   -> Json.obj(
            "serviceName"                  -> "Foo Service",
            "title"                        -> "Upload foo files",
            "descriptionHtml"              -> "\n<p class=\"govuk-body govuk-!-margin-bottom-2\">\n    Foo file can be up to a maximum of 9 MB size per file. The selected file must be Excel, Outlook, JPG, PNG, PDF, CSV, TXT or Word.\n</p>\n\n\n<p class=\"govuk-body govuk-!-margin-bottom-6\">\n    You can use the 'Choose files' button to upload or 'drag and drop' multiple files.\n</p>\n",
            "serviceUrl"                   -> appConfig.backLinkUrl,
            "accessibilityStatementUrl"    -> "http://localhost:12346/accessibility-statement/claim-back-import-duty-vat",
            "phaseBanner"                  -> "beta",
            "phaseBannerUrl"               -> "https://www.staging.tax.service.gov.uk/contact/beta-feedback?service=CDSRC",
            "signOutUrl"                   -> s"http://localhost:9553/bas-gateway/sign-out-without-state?continue=${appConfig.backLinkUrl}",
            "timedOutUrl"                  -> s"http://localhost:9553/bas-gateway/sign-out-without-state?continue=${appConfig.backLinkUrl}",
            "keepAliveUrl"                 -> appConfig.backLinkUrl,
            "timeoutSeconds"               -> 3600,
            "countdownSeconds"             -> 120,
            "pageTitleClasses"             -> "govuk-heading-xl",
            "allowedFilesTypesHint"        -> "Excel, Outlook, JPG, PNG, PDF, CSV, TXT or Word",
            "fileUploadedProgressBarLabel" -> "Uploaded",
            "chooseFirstFileLabel"         -> "Upload foo file",
            "chooseNextFileLabel"          -> "Upload another foo file",
            "addAnotherDocumentButtonText" -> "Upload another foo file",
            "yesNoQuestionText"            -> "Add a different type of a foo file?",
            "yesNoQuestionRequiredError"   -> "Select yes to add a different type of a foo file"
          ),
          "features"                  -> Json.obj(
            "showUploadMultiple"              -> true,
            "showLanguageSelection"           -> true,
            "showAddAnotherDocumentButton"    -> false,
            "showYesNoQuestionBeforeContinue" -> false,
            "enableMultipleFilesPicker"       -> true
          )
        )
      )
      .deepMerge(previousFiles)
  }

  def defaultConfig(
    maybePreviousFiles: Option[UploadedFilesCallback]
  )(implicit appConfig: AppConfig): InitialisationModel =
    InitialisationModel(
      Json.prettyPrint(minimumJson(maybePreviousFiles)),
      appConfig.defaultUserAgent,
      appConfig.uploadCustomsDocumentsDNS
    )
}
