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

package connectors.mocks

import izumi.reflect.Tag
import org.scalamock.handlers.CallHandler2
import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.libs.ws.BodyWritable
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.client.RequestBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads
import uk.gov.hmrc.http.*

import java.net.URL
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait MockHttp extends MockFactory {
  this: TestSuite =>

  val mockHttp: HttpClientV2             = mock[HttpClientV2]
  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]

  def setupMockHttpPost[I, O](url: String, model: I, headers: Seq[(String, String)])(response: O): Unit =
    mockHttpPost(URL(url)).once()
    mockRequestBuilderWithBody(model).once()
    if headers.nonEmpty then mockRequestBuilderTransform(headers).once()
    mockRequestBuilderTransform().once()
    mockRequestBuilderExecuteWithoutException(response).once()

  def mockHttpPost[A](url: URL) =
    (mockHttp
      .post(_: URL)(_: HeaderCarrier))
      .expects(url, *)
      .returning(mockRequestBuilder)

  def mockRequestBuilderWithBody[I](
    body: I
  ): CallHandler4[I, BodyWritable[I], Tag[I], ExecutionContext, RequestBuilder] =
    (mockRequestBuilder
      .withBody(_: I)(using _: BodyWritable[I], _: Tag[I], _: ExecutionContext))
      .expects(body, *, *, *)
      .returning(mockRequestBuilder)

  def mockRequestBuilderTransform(headers: Seq[(String, String)]) =
    (mockRequestBuilder
      .transform(_: WSRequest => WSRequest))
      .expects(*)
      .returning(mockRequestBuilder.transform(_.addHttpHeaders(headers*)))

  def mockRequestBuilderTransform() =
    (mockRequestBuilder
      .transform(_: WSRequest => WSRequest))
      .expects(*)
      .returning(mockRequestBuilder)

  def mockRequestBuilderExecuteWithoutException[O](
    value: O
  ): CallHandler2[HttpReads[O], ExecutionContext, Future[O]] =
    (mockRequestBuilder
      .execute(using _: HttpReads[O], _: ExecutionContext))
      .expects(*, *)
      .returning(Future.successful(value))

}
