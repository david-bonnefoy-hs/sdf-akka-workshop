package com.boldradius.sdf.akka

import akka.actor.SupervisorStrategy.{Decider, Restart}
import akka.actor._
import com.boldradius.sdf.akka.EmailActor.EmailMessage
import com.boldradius.sdf.akka.RealTimeStatAggregator.StatType
import com.boldradius.sdf.akka.RequestConsumer.{GetRealTimeStatistics, FailAggregator}
import com.boldradius.sdf.akka.StatsAggregatorActor.ForceFailure

import scala.concurrent.duration._

/**
 * Created by davidb on 15-06-24.
 */
class RequestConsumer(maxAggregatorFailureCount: Int, emailActor: ActorRef) extends Actor with ActorLogging {
  import RequestConsumer._

  val aggregator = createAggregator
  val chatActor = createChatActor
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
        context.actorOf(SessionTracker.props(request.sessionId, sessionTimeout = 20 second, helpTimeout = 10 second, chatActor), monitorName)
      }
      tracker ! request
      aggregator ! request

    case getStats: GetRealTimeStatistics =>
      context.actorOf(RealTimeStatAggregator.props(allSessionTrackers, getStats.statType, sender()))

    case FailAggregator =>
      aggregator ! ForceFailure
  }

  def createAggregator: ActorRef = {
    context.actorOf(StatsAggregatorActor.props(), "aggregator")
  }

  def createChatActor: ActorRef = {
    context.actorOf(ChatActor.props)
  }

  def allSessionTrackers =
    context.children.filterNot(actor => actor == aggregator || actor == chatActor)
}

object RequestConsumer
{
  def props(maxAggregatorFailureCount: Int, emailActor: ActorRef) =
    Props(new RequestConsumer(maxAggregatorFailureCount, emailActor))

  case object FailAggregator

  case class GetRealTimeStatistics(statType: StatType)
}

