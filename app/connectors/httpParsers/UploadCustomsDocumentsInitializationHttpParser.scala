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

import play.api.http.HeaderNames
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggerUtil

object UploadCustomsDocumentsInitializationHttpParser extends LoggerUtil {

  type UploadCustomsDocumentsInitializationResponse = Either[ErrorResponse, String]

  implicit object UploadCustomsDocumentsInitializationReads extends HttpReads[UploadCustomsDocumentsInitializationResponse] {

    def read(method: String, url: String, response: HttpResponse): UploadCustomsDocumentsInitializationResponse = {
      response.status match {
        case CREATED =>
          response.header(HeaderNames.LOCATION) match {
            case Some(url) => Right(url)
            case None =>
              logger.debug(s"[read]: No Location Header in response: ${response.headers}")
              logger.warn(s"[read]: No Location Header returned from Upload Customs Documents Frontend")
              Left(NoLocationHeaderReturned)
          }
        case status =>
          logger.warn(s"[read]: Unexpected response, status $status returned")
          Left(UnexpectedFailure(status))
      }
    }
  }

  trait ErrorResponse {
    val status: Int
    val body: String
  }

  object NoLocationHeaderReturned extends ErrorResponse {
    override val status: Int = INTERNAL_SERVER_ERROR
    override val body: String = "Upload Customs Documents Frontend returned CREATED (201) but did not provide a Location header"
  }
  case class UnexpectedFailure(override val status: Int) extends ErrorResponse {
    override val body: String = s"Unexpected response, status $status returned"
  }

}
