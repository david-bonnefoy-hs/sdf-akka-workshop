package com.boldradius.sdf.akka

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import scala.concurrent.duration._

import scala.io.StdIn

object ChatSystemApp extends App {

  val appConfig = ConfigFactory.load

  // Setup with overwriting configuration
  def remoteConfig(hostname: String, port: Int, commonConfig: Config): Config = {
    val configStr =
      "akka.remote.netty.tcp.hostname = " + hostname + "\n" +
        "akka.remote.netty.tcp.port = " + port + "\n"

    ConfigFactory.parseString(configStr).withFallback(commonConfig)
  }
  val system = ActorSystem("ChatSystem", remoteConfig("127.0.0.1", 2553, appConfig))

  // Setup with default configuration and command line parameters
  //val system = ActorSystem("ChatSystem")

  val chatActor = system.actorOf(ChatActor.props, "ChatActor")

  // Wait for the user to hit <enter>
  println("Hit <enter> to stop the chat system")
  StdIn.readLine()


  // Terminate all actors and wait for graceful shutdown
  system.shutdown()
  system.awaitTermination(10 seconds)

}