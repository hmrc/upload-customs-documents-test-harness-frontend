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

package utils

import ch.qos.logback.classic.Level
import org.scalatestplus.play.PlaySpec

class LoggerUtilSpec extends PlaySpec with LogCapturing {

  object TestInstanceWithLogging extends LoggerUtil {

    def generateSomeLogs() = {
      logger.debug("debug-bar")
      logger.info("info-bar")
      logger.warn("warn-bar")
      logger.error("error-bar")
    }
  }


  "LoggerUtil" must {

    "Provide a logger which" must {

      "have the correct underlying logger for the class" in {

        TestInstanceWithLogging.logger.logger.getName mustBe "application.TestInstanceWithLogging"
      }

      Seq(Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR) foreach { level =>

        s"output the correct $level level log and prefix with the class name" in {

          withCaptureOfLoggingFrom(TestInstanceWithLogging.logger) { logs =>

            TestInstanceWithLogging.generateSomeLogs()

            logs.find(_.getLevel == level) match {
              case Some(value) => value.getMessage mustBe s"[TestInstanceWithLogging] ${level.toString.toLowerCase}-bar"
              case None => fail(s"Could not find $level message")
            }
          }
        }
      }
    }
  }
}
