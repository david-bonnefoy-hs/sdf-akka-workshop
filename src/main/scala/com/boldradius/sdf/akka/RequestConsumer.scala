package com.boldradius.sdf.akka

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import akka.actor.Actor.Receive

/**
 * Created by davidb on 15-06-24.
 */
class RequestConsumer extends Actor with ActorLogging {
  override def receive: Receive = {
    case request: Request => log.info("Consumer received {}", request)
  }
}

object RequestConsumer {

  def props =
    Props(new RequestConsumer)
}

