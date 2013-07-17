name := "sortable-matching-challenge"

version := "0.1"

scalaVersion := "2.10.1"

resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "spray repo" at "http://repo.spray.io"
)

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= Seq( 
    "com.typesafe.akka" %% "akka-actor" % "2.1.4",
    "com.github.nscala-time" %% "nscala-time" % "0.4.2",
    "io.spray" %%  "spray-json" % "1.2.5"
)
