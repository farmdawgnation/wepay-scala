name := "wepay-scala"

version := "0.8"

scalaVersion := "2.9.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies ++= Seq(
  "net.liftweb" % "lift-common_2.9.2" % "2.5-SNAPSHOT",
  "net.liftweb" % "lift-util_2.9.2" % "2.5-SNAPSHOT",
  "net.liftweb" % "lift-json_2.9.2" % "2.5-SNAPSHOT",
  "net.databinder" % "dispatch-core_2.9.2" % "0.8.8",
  "net.databinder" % "dispatch-lift-json_2.9.1" % "0.8.5"
)
