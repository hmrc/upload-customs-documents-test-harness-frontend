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

import connectors.httpParsers.UploadCustomsDocumentsInitializationHttpParser.UploadCustomsDocumentsInitializationReads
import connectors.httpParsers.UploadCustomsDocumentsInitializationHttpParser.UploadCustomsDocumentsInitializationResponse
import models.InitialisationModel
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import utils.LoggerUtil

import java.net.URL
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class UploadCustomsDocumentsConnector @Inject() (http: HttpClientV2) extends LoggerUtil {

  def initialize(
    configuration: InitialisationModel
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[UploadCustomsDocumentsInitializationResponse] = {

    val url = URL(configuration.url + "/internal/initialize")
    logger.debug(s"URL: $url, Body: \n\n${configuration.json}")

    http
      .post(url)
      .withBody(Json.parse(configuration.json))
      .transform(_.addHttpHeaders(HeaderNames.USER_AGENT -> configuration.userAgent))
      .execute[UploadCustomsDocumentsInitializationResponse]

  }
}
