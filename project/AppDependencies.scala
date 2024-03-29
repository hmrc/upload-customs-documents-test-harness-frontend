import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28" % "7.3.0",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"         % "3.24.0-play-28",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"         % "0.73.0"
  )

  val test = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-28"  % "7.3.0"  % "test, it",
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-test-play-28" % "0.73.0" % Test,
    "org.jsoup"            % "jsoup"                   % "1.15.3" % Test,
    "com.vladsch.flexmark" % "flexmark-all"            % "0.36.8" % "test, it",
    "org.scalamock"       %% "scalamock"               % "5.1.0"  % Test
  )
}
