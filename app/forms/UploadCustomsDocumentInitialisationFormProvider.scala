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

package forms

import models.InitialisationModel
import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.libs.json.{JsValue, Json}

import javax.inject.Inject
import scala.util.Try

class UploadCustomsDocumentInitialisationFormProvider @Inject()() {

  val isJson: Constraint[String] = Constraint("isJson")({ plainText =>
    if (Try(Json.parse(plainText)).isSuccess) Valid else Invalid("Not Valid JSON!")
  })

  def apply(): Form[InitialisationModel] =
    Form(mapping(
      "json" -> text.verifying(isJson)
        .transform[JsValue](Json.parse, _.toString)
    )(InitialisationModel.apply)(InitialisationModel.unapply))
}
