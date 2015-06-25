package com.boldradius.sdf.akka

import akka.actor.Actor.Receive
import akka.actor._
import akka.util.Timeout
import com.boldradius.sdf.akka.RealTimeStatAggregator.StatType
import com.boldradius.sdf.akka.SessionTracker.LastRequest
import scala.collection.mutable
import akka.pattern.ask
import akka.pattern.pipe
import scala.concurrent.duration.DurationInt

/**
 * Created by davidb on 15-06-25.
 */
class RealTimeStatAggregator(sessionActors: Iterable[ActorRef], statType: StatType, requester: ActorRef) extends Actor with ActorLogging {

  import context.dispatcher
  implicit val timeout = 10 seconds: Timeout

  val nbResponseExpected = sessionActors.size
  var nbResponseReceived = 0
  val requestReceived = mutable.Set[Request]()

  for (sessionTracker <- sessionActors)
    sessionTracker ? LastRequest pipeTo self

  override def receive: Receive = {
    case request: Request =>
      requestReceived += request
      processStatistics()
    case Status.Failure =>
      processStatistics()
  }

  def processStatistics() = {
    nbResponseReceived += 1
    if (nbResponseReceived == nbResponseExpected) {
      val responseMessage = statType match {
        case userPerBrowser => requestReceived.groupBy(_.browser).mapValues(_.size)
      }
      requester ! responseMessage
      context.stop(self)
    }
  }
}

object RealTimeStatAggregator {

  sealed trait StatType
  object usersPerBrowser extends StatType

  def props(sessionActors: Iterable[ActorRef], statType: StatType, requester: ActorRef) = Props(new RealTimeStatAggregator(sessionActors, statType, requester))
}