import sbt._
import sbt.Keys._

object Build extends Build {

  val commonSettings = Seq(
    organization := "com.gu",
    scalaVersion := "2.10.2",
    scalacOptions ++= Seq("-feature", "-deprecation", "-language:higherKinds", "-Xfatal-warnings")
  )

  val scalazDependencies = Seq(
    "org.scalaz" %% "scalaz-core" % "7.1.0-M3",
    "org.scalaz" %% "scalaz-effect" % "7.1.0-M3"
  )

  val commonsNetDependencies = Seq("commons-net" % "commons-net" % "3.3")

  val algebra = Project("algebra", file("algebra"))
    .settings(commonSettings: _*)
    .settings(
      name := "ftp-algebra",
      libraryDependencies ++= scalazDependencies
    )

  val interpreterCommonsNet = Project("interpreter-commons-net", file("interpreter-commons-net"))
    .dependsOn(algebra)
    .settings(commonSettings: _*)
    .settings(
      name := "ftp-interpreter-commons-net",
      libraryDependencies ++= commonsNetDependencies
    )

}
