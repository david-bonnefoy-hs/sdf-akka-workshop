package com.boldradius.sdf.akka

import akka.actor.Actor.Receive
import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import com.boldradius.sdf.akka.ChatActor.HelpRequested

/**
 * Created by davidb on 15-06-26.
 */
class ChatActor  extends Actor with ActorLogging {

  println("XXX Chat actor created")

  override def receive: Receive = {
    case HelpRequested(userId) =>
      println(s"XXX User $userId requested help.")
      log.info(s"User $userId requested help.")
  }
}

object ChatActor {

  case class HelpRequested(userId: Long)

  def props = Props(new ChatActor())

}