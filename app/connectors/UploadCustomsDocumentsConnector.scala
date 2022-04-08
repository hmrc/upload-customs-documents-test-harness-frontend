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

import config.AppConfig
import connectors.httpParsers.UploadCustomsDocumentsInitializationHttpParser.{UploadCustomsDocumentsInitializationReads, UploadCustomsDocumentsInitializationResponse}
import models.InitialisationModel
import play.api.http.HeaderNames
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.LoggerUtil

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UploadCustomsDocumentsConnector @Inject()(httpClient: HttpClient, implicit val appConfig: AppConfig) extends LoggerUtil {

  //TODO: Pass through a model representing all the configurable parameters
  def initialize(configuration: InitialisationModel)
                (implicit hc: HeaderCarrier, ec: ExecutionContext, messages: MessagesApi): Future[UploadCustomsDocumentsInitializationResponse] = {

    val url = appConfig.uploadCustomsDocumentsDNS + "/internal/initialize"
    logger.debug(s"URL: $url, Body: \n\n${configuration.json}")

    httpClient.POST(
      url = url,
      body = configuration.json,
      headers = Seq(HeaderNames.USER_AGENT -> configuration.userAgent)
    )(implicitly, UploadCustomsDocumentsInitializationReads, hc, ec)
  }
}
