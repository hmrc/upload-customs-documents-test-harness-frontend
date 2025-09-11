import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "upload-customs-documents-test-harness-frontend"

Global / semanticdbEnabled := true

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.6"
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

lazy val microservice = Project(appName, file("."))
  .enablePlugins(
    play.sbt.PlayScala,
    SbtDistributablesPlugin
  )
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(scalafmtOnCompile := true)
  .settings(PlayKeys.playDefaultPort := 10111)
  .settings(Assets / pipelineStages := Seq(uglify))
  .settings(uglifyOps := UglifyOps.singleFile)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions += s"-Wconf:src=${target.value}/scala-${scalaBinaryVersion.value}/routes/.*:s,src=${target.value}/scala-${scalaBinaryVersion.value}/twirl/.*:s"
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.itDependencies)
