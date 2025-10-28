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

import base.SpecBase
import models.InitialisationModel
import play.api.data.Form
import play.api.data.FormError

class UploadCustomsDocumentInitialisationFormProviderSpec extends SpecBase {

  val form = new UploadCustomsDocumentInitialisationFormProvider()()

  "binding data" when {

    "data is not JSON" must {

      "return form error" in {

        val result: Form[InitialisationModel] = form.bind(Map("json" -> "", "userAgent" -> "foo", "url" -> "someUrl"))

        result.hasErrors mustBe true
        result.error("json") mustBe Some(FormError("json", Seq("Not Valid JSON!")))
      }
    }

    "data is JSON" must {

      "return form" in {

        val result: Form[InitialisationModel] = form.bind(Map("json" -> "{}", "userAgent" -> "foo", "url" -> "someUrl"))

        result.hasErrors mustBe false
        result.value mustBe Some(InitialisationModel("{}", "foo", "someUrl"))
      }
    }

    "userAgent is missing" must {

      "return form" in {

        val result: Form[InitialisationModel] = form.bind(Map("json" -> "{}", "url" -> "someUrl"))

        result.hasErrors mustBe true
        result.error("userAgent") mustBe Some(FormError("userAgent", Seq("error.required")))
      }
    }

    "url is missing" must {

      "return form" in {

        val result: Form[InitialisationModel] = form.bind(Map("json" -> "{}", "userAgent" -> "foo"))

        result.hasErrors mustBe true
        result.error("url") mustBe Some(FormError("url", Seq("error.required")))
      }
    }
  }
}
