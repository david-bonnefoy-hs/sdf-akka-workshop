name := """RequestProducer"""

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"      % "2.3.9",
  "com.typesafe.akka" %% "akka-testkit"    % "2.3.9" % "test",
  "org.scalatest"     %% "scalatest"       % "2.2.4" % "test",
  "com.typesafe.akka" %% "akka-slf4j"      % "2.3.6",
  "com.typesafe.akka" %% "akka-testkit"    % "2.3.6",
  "ch.qos.logback"    %  "logback-classic" % "1.1.2",
  "com.typesafe.akka" %% "akka-remote"     % "2.4-M1"
)

addCommandAlias("client", "runMain com.boldradius.sdf.akka.RequestSimulationExampleApp -Dakka.remote.netty.tcp.port=2552")
addCommandAlias("chat", "runMain com.boldradius.sdf.akka.ChatSystemApp -Dakka.remote.netty.tcp.port=2553")