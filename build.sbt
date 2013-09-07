name := "wepay-scala"

description := "A scala library for WePay payments processing."

organization := "me.frmr.wepay-scala"

version := "0.11.0-SNAPSHOT"

pomExtra :=
<url>http://wepay-scala.frmr.me</url>
<licenses>
  <license>
    <name>Apache 2</name>
    <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    <distribution>repo</distribution>
  </license>
</licenses>
<scm>
  <url>https://github.com/farmdawgnation/wepay-scala.git</url>
  <connection>https://github.com/farmdawgnation/wepay-scala.git</connection>
</scm>
<developers>
  <developer>
    <id>farmdawgnation</id>
    <name>Matt Farmer</name>
    <email>matt@frmr.me</email>
  </developer>
</developers>

scalaVersion := "2.10.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-common" % "2.5+",
  "net.liftweb" %% "lift-util" % "2.5+",
  "net.liftweb" %% "lift-json" % "2.5+",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "net.databinder.dispatch" %% "dispatch-lift-json" % "0.11.0" exclude("net.liftweb", "lift-json"),
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.1",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

scalacOptions in (Compile, doc) ++= Opts.doc.title("WePay-Scala API Reference")

scalacOptions += "-deprecation"

scalacOptions += "-feature"

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) 
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".sonatype")

parallelExecution in Test := false

mappings in (Compile, packageBin) ~= { (ms: Seq[(File, String)]) =>
  ms filter {
    case (file, toPath) => {
      val shouldExclude = """(.*?)\.(properties|props|conf|dsl|txt|xml)$""".r.pattern.matcher(file.getName).matches
      ! shouldExclude
    }
  }
}
