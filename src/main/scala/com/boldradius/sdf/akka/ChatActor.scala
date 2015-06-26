package com.boldradius.sdf.akka

import akka.actor.Actor.Receive
import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import com.boldradius.sdf.akka.ChatActor.HelpRequested

/**
 * Created by davidb on 15-06-26.
 */
class ChatActor  extends Actor with ActorLogging {
  override def receive: Receive = {
    case HelpRequested(userId) => log.info(s"User $userId requested help.")
  }
}

object ChatActor {

  case class HelpRequested(userId: Long)

  def props = Props(new ChatActor())

}