package com.boldradius.sdf.akka

import java.util.concurrent.TimeUnit

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import akka.actor.Actor.Receive

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.FiniteDuration

/**
 * Created by davidb on 15-06-24.
 */
class RequestConsumer extends Actor with ActorLogging {

  val receivedRequests: ListBuffer[Request] = new ListBuffer[Request]
  val sessionTrackers = scala.collection.mutable.Map.empty[Long, ActorRef]withDefault(
    sessionId => context.actorOf(SessionTracker.props(sessionId, FiniteDuration(20, TimeUnit.SECONDS))))

  override def receive: Receive = {
    case request: Request =>
      log.info("Consumer received {}", request)
      receivedRequests += request
      sessionTrackers(request.sessionId) ! request
  }
}

object RequestConsumer {

  def props =
    Props(new RequestConsumer)
}

