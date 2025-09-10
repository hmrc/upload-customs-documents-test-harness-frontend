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

package base

import config.AppConfig
import org.scalatestplus.play.guice._
import play.api.Application
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.MessagesControllerComponents
import play.api.test.Injecting

import scala.concurrent.ExecutionContext

trait GuicySpec extends SpecBase with GuiceOneAppPerSuite with Injecting {

  override lazy val app: Application = GuiceApplicationBuilder().build()

  implicit lazy val appConfig: AppConfig     = inject[AppConfig]
  implicit lazy val ec: ExecutionContext     = inject[ExecutionContext]
  lazy val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  implicit lazy val messagesApi: MessagesApi = inject[MessagesApi]
  implicit lazy val messages: Messages       = messagesApi.preferred(fakeRequest)
}
