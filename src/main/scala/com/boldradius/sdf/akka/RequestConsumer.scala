package com.boldradius.sdf.akka

import akka.actor.SupervisorStrategy.{Stop, Restart, Decider}
import akka.actor._
import com.boldradius.sdf.akka.EmailActor.EmailMessage
import com.boldradius.sdf.akka.RequestConsumer.FailAggregator
import com.boldradius.sdf.akka.StatsAggregatorActor.ForceFailure

import scala.concurrent.duration._

/**
 * Created by davidb on 15-06-24.
 */
class RequestConsumer(maxAggregatorFailureCount: Int, emailActor: ActorRef) extends Actor with ActorLogging {

  val aggregator = createAggregator
  var aggregatorFailureCount = 0

  override val supervisorStrategy: SupervisorStrategy = {
    def aggregatorDecider: Decider = {
      case _ if sender() == aggregator =>
        aggregatorFailureCount += 1
        if (aggregatorFailureCount >= maxAggregatorFailureCount) {
          aggregatorFailureCount = 0
          emailActor ! EmailMessage("Aggregator agent failed")
        }
        Restart
    }
    OneForOneStrategy()(aggregatorDecider orElse super.supervisorStrategy.decider)
  }

  override def receive: Receive = {
    case request: Request =>
      log.info("Consumer received {}", request)

      val monitorName: String = request.sessionId.toString
      val tracker = context.child(monitorName) getOrElse {
        context.actorOf(SessionTracker.props(request.sessionId, 20 second), monitorName)
      }
      tracker ! request
      aggregator ! request

    case FailAggregator =>
      aggregator ! ForceFailure
  }

  def createAggregator: ActorRef = {
    context.actorOf(StatsAggregatorActor.props(), "aggregator")
  }
}

object RequestConsumer
{
  def props(maxAggregatorFailureCount: Int, emailActor: ActorRef) =
    Props(new RequestConsumer(maxAggregatorFailureCount, emailActor))

  case object FailAggregator
}

