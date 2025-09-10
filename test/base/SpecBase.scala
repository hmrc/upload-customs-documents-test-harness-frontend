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

import ch.qos.logback.classic.spi.ILoggingEvent
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.Assertion
import org.scalatest.BeforeAndAfterEach
import org.scalatest.TryValues
import org.scalatestplus.play.PlaySpec
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.DefaultAwaitTimeout
import play.api.test.FakeRequest
import play.api.test.FutureAwaits
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.SessionKeys

import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future

trait SpecBase
    extends PlaySpec
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MaterializerSupport
    with BeforeAndAfterEach
    with FutureAwaits
    with DefaultAwaitTimeout {

  private def makeFakeRequest(sessionKvs: (String, String)*) = {
    val session: Seq[(String, String)] = sessionKvs.toSeq :+ SessionKeys.sessionId -> "foo"
    FakeRequest("GET", "/foo").withSession(session: _*).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
  }

  def fakeRequest(sessionKvs: (String, String)*): FakeRequest[AnyContentAsEmpty.type] = makeFakeRequest(sessionKvs: _*)

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = makeFakeRequest()

  implicit val defaultTimeout: FiniteDuration = 5.seconds

  def await[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  implicit class LogCapturingExtensions(logs: List[ILoggingEvent]) {
    def includes(msg: String): Assertion =
      logs.map(_.getMessage).mkString("\n") must include(msg)
  }
}
