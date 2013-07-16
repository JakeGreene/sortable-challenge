name := "sortable-matching-challenge"

version := "0.1"

scalaVersion := "2.10.1"

resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "spray repo" at "http://repo.spray.io"
)

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= Seq( 
    "com.typesafe.akka" %% "akka-actor" % "2.1.4",
    "io.spray" %%  "spray-json" % "1.2.5"
)
