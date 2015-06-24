package com.boldradius.sdf.akka

import akka.actor.{Actor, ActorLogging, Props}

import scala.concurrent.duration._

/**
 * Created by davidb on 15-06-24.
 */
class RequestConsumer extends Actor with ActorLogging {

  val aggregator = context.actorOf(StatsAggregatorActor.props())

  override def receive: Receive = {
    case request: Request =>
      log.info("Consumer received {}", request)

      val monitorName: String = request.sessionId.toString
      val tracker = context.child(monitorName) getOrElse {
        context.actorOf(SessionTracker.props(request.sessionId, 20 second), monitorName)
      }
      tracker ! request
      aggregator ! request
  }
}

object RequestConsumer
{
  def props =
    Props(new RequestConsumer)
}

