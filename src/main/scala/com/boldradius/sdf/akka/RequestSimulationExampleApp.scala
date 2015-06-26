package com.boldradius.sdf.akka

import akka.actor.{ActorRef, ActorSystem}
import com.boldradius.sdf.akka.ChatActor.HelpRequested
import com.boldradius.sdf.akka.RequestProducer._
import scala.concurrent.Await
import scala.io.StdIn
import scala.concurrent.duration._

object RequestSimulationExampleApp extends App {

  // First, we create an actor system, a producer and a consumer
  val system = ActorSystem("EventProducerExample")

  val chatActor = findChatActor

  val emailActor = system.actorOf(EmailActor.props)
  val producer = system.actorOf(RequestProducer.props(100), "producerActor")
  val consumer = system.actorOf(RequestConsumer.props(5, emailActor, chatActor))

  // Tell the producer to start working and to send messages to the consumer
  producer ! Start(consumer)

  // Wait for the user to hit <enter>
  println("Hit <enter> to stop the simulation")
  StdIn.readLine()

  // Tell the producer to stop working
  producer ! Stop

  // Terminate all actors and wait for graceful shutdown
  system.shutdown()
  system.awaitTermination(10 seconds)

  def findChatActor: ActorRef = {
    println("XXX Looking for chat actor")
    val selection =
      system.actorSelection("akka.tcp://ChatSystem@127.0.0.1:2553/user/ChatActor")
    val chatActor = Await.result(selection.resolveOne(10 second), 10 second)
    println(s"XXX Returning chat actor $chatActor")
    chatActor
  }
}
