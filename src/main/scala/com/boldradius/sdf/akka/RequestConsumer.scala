package com.boldradius.sdf.akka

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import akka.actor.Actor.Receive

import scala.collection.mutable.ListBuffer

/**
 * Created by davidb on 15-06-24.
 */
class RequestConsumer extends Actor with ActorLogging {

  val receivedRequests: ListBuffer[Request] = new ListBuffer[Request]

  override def receive: Receive = {
    case request: Request =>
      log.info("Consumer received {}", request)
      receivedRequests += request
  }
}

object RequestConsumer {

  def props =
    Props(new RequestConsumer)
}

