/*
 * Copyright 2023 HM Revenue & Customs
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

import controllers.UpscanController
import play.api.Configuration
import play.api.libs.json.*
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton()
class UpscanInternalController @Inject() (
  mcc: MessagesControllerComponents,
  configuration: Configuration
) extends FrontendController(mcc) {

  val uploadFileBaseUrl = configuration.get[String]("upscanStubHostUrl")

  final val prepareUpload: Action[AnyContent] =
    Action { request =>
      request.body.asJson
        .map(_.as[UpscanController.UpscanInitiateRequest])
        .match {
          case None                        => BadRequest
          case Some(upscanInitiateRequest) =>
            val upscanReference = UUID.randomUUID().toString()
            UpscanController.upscanInitiateRequestCache.put(upscanReference, upscanInitiateRequest)

            Ok(Json.parse(s"""{
    "reference": "$upscanReference",
    "uploadRequest": {
        "href": "$uploadFileBaseUrl${controllers.routes.UpscanUploadController.upload.url}",
        "fields": {
            "acl": "private",
            "key": "$upscanReference",
            "x-amz-date": "${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}Z",
            "x-amz-algorithm": "AWS4-HMAC-SHA256",
            "x-amz-credential": "ASIAxxxxxxxxx/20180202/eu-west-2/s3/aws4_request",
            "x-amz-meta-callback-url": "${upscanInitiateRequest.callbackUrl}",
            "success_action_redirect": "${upscanInitiateRequest.successRedirect}",
            "error_action_redirect": "${upscanInitiateRequest.errorRedirect}",
            "x-amz-meta-upscan-initiate-response": "dummy",
            "x-amz-meta-upscan-initiate-received": "dummy",
            "x-amz-meta-request-id": "${UUID.randomUUID().toString()}",
            "x-amz-signature": "xyz",
            "x-amz-meta-session-id": "${UUID.randomUUID().toString()}",
            "policy": "xyz",
            "x-amz-meta-original-filename": "foo.bar"
        }
    }
}"""))
        }
    }

}
