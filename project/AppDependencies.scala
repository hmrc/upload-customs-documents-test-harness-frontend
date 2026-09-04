import sbt._

object AppDependencies {

  val hmrcMongoPlayVersion = "2.13.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30" % "10.7.0",
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30" % "13.11.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % hmrcMongoPlayVersion
  )

  val test = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-30"  % "10.7.0"             % Test,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-test-play-30" % hmrcMongoPlayVersion % Test,
    "org.jsoup"            % "jsoup"                   % "1.22.2"             % Test,
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.8"             % Test,
    "org.scalamock"       %% "scalamock"               % "7.5.5"              % Test
  )

  val itDependencies = Seq()
}
