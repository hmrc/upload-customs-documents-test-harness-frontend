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

package controllers

import controllers.UpscanController.UpscanInitiateRequest
import org.apache.pekko.actor.ActorSystem
import play.api.Configuration
import play.api.libs.Files
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.*
import play.api.libs.ws.WSClient
import play.api.libs.ws.writeableOf_String
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class UpscanUploadController @Inject() (
  mcc: MessagesControllerComponents,
  wsClient: WSClient,
  configuration: Configuration
)(implicit
  ec: ExecutionContext
) extends FrontendController(mcc) {

  val downloadFileBaseUrl = configuration.get[String]("downloadFileUrl")

  val upload: Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { request =>
    val body = request.body
    body.dataParts
      .get("key")
      .map { upscanReferences =>
        println("upscanReferences: " + upscanReferences)
        upscanReferences.headOption.match {
          case None                  => BadRequest
          case Some(upscanReference) =>
            body
              .file("file")
              .map { file =>
                Option(UpscanController.upscanInitiateRequestCache.get(upscanReference)).match {
                  case None                        => BadRequest
                  case Some(upscanInitiateRequest) =>
                    UpscanController.fileUploadsCache.put(upscanReference, file.ref)
                    Future {
                      Thread.sleep(5000)
                      println("filename: " + file.filename)
                      file.filename.match {
                        case "TestPicture.png"       =>
                          sendRejectedTooLargeResponse(upscanInitiateRequest, upscanReference, file)
                        case "fakeVirusTestFile.png" =>
                          sendRejectedVirusResponse(upscanInitiateRequest, upscanReference)
                        case "empty.png"             =>
                          sendInvalidArgumentResponse(upscanInitiateRequest, upscanReference)
                        case _                       =>
                          sendDefaultResponse(upscanInitiateRequest, upscanReference, file)
                      }
                    }
                    Redirect(
                      Call(
                        "GET",
                        upscanInitiateRequest.successRedirect + s"?key=$upscanReference&bucket=dummy"
                      )
                    )
                }
              }
              .getOrElse(
                Option(UpscanController.upscanInitiateRequestCache.get(upscanReference)).match {
                  case None                        => BadRequest
                  case Some(upscanInitiateRequest) =>
                    println("no file found")
                    Future {
                      Thread.sleep(5000)
                      sendInvalidArgumentResponse(upscanInitiateRequest, upscanReference)
                    }
                    Redirect(Call("GET", upscanInitiateRequest.successRedirect + s"?key=$upscanReference"))
                }
              )
        }
      }
      .getOrElse(BadRequest)
  }

  final def download(upscanReference: String): Action[AnyContent] =
    Action { _ =>
      Option(UpscanController.fileUploadsCache.get(upscanReference)).match {
        case None          => BadRequest
        case Some(fileRef) =>
          Ok(java.nio.file.Files.readAllBytes(fileRef.path))
      }
    }

  private def sendRejectedTooLargeResponse(
    upscanInitiateRequest: UpscanInitiateRequest,
    upscanReference: String,
    file: MultipartFormData.FilePart[TemporaryFile]
  ): Unit =
    wsClient
      .url(upscanInitiateRequest.errorRedirect)
      .withMethod("GET")
      .withHttpHeaders(
        "Content-Type" -> "application/json"
      )
      .withQueryStringParameters(
        "errorMessage"   -> "Your proposed upload exceeds the maximum allowed size",
        "key"            -> s"$upscanReference",
        "errorCode"      -> "EntityTooLarge",
        "errorRequestId" -> "SomeRequestId",
        "errorResource"  -> s"${file.ref}"
      )
      .execute()

  private def sendInvalidArgumentResponse(
    upscanInitiateRequest: UpscanInitiateRequest,
    upscanReference: String
  ): Unit =
    wsClient
      .url(upscanInitiateRequest.errorRedirect)
      .withMethod("GET")
      .withHttpHeaders(
        "Content-Type" -> "application/json"
      )
      .withQueryStringParameters(
        "errorMessage"   -> "'file' field not found",
        "key"            -> s"$upscanReference",
        "errorCode"      -> "InvalidArgument",
        "errorRequestId" -> "SomeRequestId",
        "errorResource"  -> s"NoFileReference"
      )
      .execute()

  private def sendRejectedVirusResponse(
    upscanInitiateRequest: UpscanInitiateRequest,
    upscanReference: String
  ): Unit =
    wsClient
      .url(upscanInitiateRequest.callbackUrl)
      .withMethod("POST")
      .withHttpHeaders(
        "Content-Type" -> "application/json"
      )
      .withBody(
        Json.prettyPrint(
          Json.obj(
            "reference"      -> JsString(upscanReference),
            "fileStatus"     -> JsString("FAILED"),
            "failureDetails" -> Json.obj(
              "failureReason" -> "QUARANTINE",
              "message"       -> "Eicar-Test-Signature"
            )
          )
        )
      )
      .execute()

  private def sendDefaultResponse(
    upscanInitiateRequest: UpscanInitiateRequest,
    upscanReference: String,
    file: MultipartFormData.FilePart[TemporaryFile]
  ): Unit =
    wsClient
      .url(upscanInitiateRequest.callbackUrl)
      .withMethod("POST")
      .withHttpHeaders(
        "Content-Type" -> "application/json"
      )
      .withBody(
        Json.prettyPrint(
          Json.obj(
            "reference"     -> JsString(upscanReference),
            "fileStatus"    -> JsString("READY"),
            "downloadUrl"   -> JsString(
              downloadFileBaseUrl + routes.UpscanUploadController.download(upscanReference).url
            ),
            "uploadDetails" -> Json.obj(
              "uploadTimestamp" -> JsString(
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z"
              ),
              "checksum"        -> JsString(SHA256(file.transformRefToBytes().toByteBuffer.array())),
              "fileName"        -> JsString(file.filename),
              "fileMimeType"    -> JsString(file.contentType.getOrElse("application/octet-stream")),
              "size"            -> JsNumber(file.fileSize)
            )
          )
        )
      )
      .execute()

}

object UpscanController {

  val upscanInitiateRequestCache = new ConcurrentHashMap[String, UpscanController.UpscanInitiateRequest]()
  val fileUploadsCache           = new ConcurrentHashMap[String, TemporaryFile]()

  case class UpscanInitiateRequest(
    callbackUrl: String,
    successRedirect: String,
    errorRedirect: String,
    minimumFileSize: Option[Int] = None,
    maximumFileSize: Option[Int] = None,
    consumingService: Option[String] = None,
    createdAt: LocalDateTime = LocalDateTime.now()
  )

  object UpscanInitiateRequest {
    implicit val format: Format[UpscanInitiateRequest] = Json.format[UpscanInitiateRequest]
  }
}

import java.security.MessageDigest

object SHA256 {

  def apply(bytes: Array[Byte]): String =
    MessageDigest
      .getInstance("SHA-256")
      .digest(bytes)
      .map("%02x".format(_))
      .mkString
}

@Singleton
class UpscanCacheScheduler @Inject() (
  actorSystem: ActorSystem
)(implicit
  ec: ExecutionContext
) {

  actorSystem.scheduler.scheduleAtFixedRate(initialDelay = 1.minute, interval = 1.hour) { () =>
    actorSystem.log.info(
      s"Cleaning up upscan stub cache. ${UpscanController.upscanInitiateRequestCache.size()} files before cleanup."
    )
    UpscanController.upscanInitiateRequestCache.entrySet().forEach { entry =>
      if (entry.getValue.createdAt.isBefore(LocalDateTime.now().minusHours(1))) {
        val upscanReference = entry.getKey()
        UpscanController.upscanInitiateRequestCache.remove(upscanReference)
        Option(UpscanController.fileUploadsCache.remove(upscanReference)).foreach { fileRef =>
          try fileRef.delete()
          catch {
            case e: Exception =>
              actorSystem.log.error(s"Error deleting file ${fileRef.path}", e)
          }
        }
      }
    }
    actorSystem.log.info(
      s"Cleaning upscan stub cache completed. ${UpscanController.upscanInitiateRequestCache.size()} files remaining after cleanup."
    )
  }
}
