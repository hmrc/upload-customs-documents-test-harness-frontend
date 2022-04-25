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

package config

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {

  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  val host: String = servicesConfig.getString("host")

  val uploadCustomsDocumentsDNS: String = servicesConfig.baseUrl("upload-customs-documents-frontend")
  val uploadDocumentsDNS: String = servicesConfig.baseUrl("upload-documents-frontend")
  val hostDNS: String = servicesConfig.baseUrl("upload-customs-documents-test-harness-frontend")
  def authStubUrl: String = servicesConfig.getString("auth-stub.url") + "?continue=" + host + controllers.routes.InitialisationController.intialiseParams.url

  //Initialisation Defaults
  def backLinkUrl: String = host + controllers.routes.InitialisationController.intialiseParams.url
  def continueUrl(nonce: Int): String = host + controllers.routes.UploadedFilesController.listFiles(nonce).url
  def callbackDNSRoute: String = hostDNS + controllers.internal.routes.UploadedFilesCallbackController.post.url
  val defaultUserAgent: String = servicesConfig.getString("defaultUserAgent")
}
