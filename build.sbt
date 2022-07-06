ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "sea-battle-load-test"
  )

val gatlingVersion = "3.5.0"

libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion,
  "io.gatling" % "gatling-test-framework" % gatlingVersion,
  "com.github.phisgr" % "gatling-grpc" % "0.12.0",
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
)

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value
)

//assembly / assemblyJarName := "load-test.jar"
//assembly / mainClass := Some("BasicSimulation")

//assembly / assemblyMergeStrategy := {
//  case "reference.conf" => MergeStrategy.concat
//  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
//  case x => MergeStrategy.first
//}

enablePlugins(GatlingPlugin)

