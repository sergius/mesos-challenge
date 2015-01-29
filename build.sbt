name := "mesos-challenge"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
"com.typesafe.akka" %% "akka-testkit"       % "2.3.9"             % "test",
"org.scalatest"     %% "scalatest"          % "2.2.3"             % "test",
"org.specs2"        %% "specs2"             % "2.4.15"            % "test"
)