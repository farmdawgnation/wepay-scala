name := "wepay-scala"

organization := "me.frmr.wepay-scala"

version := "0.8.1-SNAPSHOT"

scalaVersion := "2.9.2"

crossScalaVersions := Seq("2.9.1", "2.9.2")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-common" % "2.5-SNAPSHOT",
  "net.liftweb" %% "lift-util" % "2.5-SNAPSHOT",
  "net.liftweb" %% "lift-json" % "2.5-SNAPSHOT",
  "net.databinder.dispatch" %% "core" % "0.9.1",
  "net.databinder.dispatch" %% "lift-json" % "0.9.1" exclude("net.liftweb", "lift-json"),
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.1"
)

publishTo := Some(Resolver.file("file", new File("../wepay-scala-repository/releases")))

scalacOptions in (Compile, doc) ++= Opts.doc.title("WePay-Scala API Reference")
