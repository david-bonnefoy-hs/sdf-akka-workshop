package com.boldradius.sdf.akka

import akka.actor.Actor.Receive
import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import com.boldradius.sdf.akka.EmailActor.EmailMessage

/**
 * Created by davidb on 15-06-25.
 */
class EmailActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case EmailMessage(message) => log.info(s"Sending mail message: $message")
  }
}

object EmailActor {

  case class EmailMessage(message: String)

  def props =
    Props(new EmailActor)
}