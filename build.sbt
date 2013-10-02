name := "ftp-algebra"

organization := "com.gu"

version := "0.1"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.0-M3",
  "org.scalaz" %% "scalaz-effect" % "7.1.0-M3"
)

scalacOptions ++= Seq("-feature", "-deprecation", "-language:higherKinds", "-Xfatal-warnings")

libraryDependencies += "commons-net" % "commons-net" % "3.3" % "test"
