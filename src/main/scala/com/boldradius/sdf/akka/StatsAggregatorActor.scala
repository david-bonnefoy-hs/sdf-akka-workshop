package com.boldradius.sdf.akka

import akka.actor.{Actor, Props}

import scala.collection.mutable.ListBuffer

/**
 * Created by jonathann on 2015-06-24.
 */
class StatsAggregatorActor extends Actor
{
  import StatsAggregatorActor._

  val receivedRequests: ListBuffer[Request] = new ListBuffer[Request]

  override def receive: Receive =
  {
    case request: Request =>
      receivedRequests += request

    case ForceFailure =>
      throw new ForcedFailureException

    case GetRequestsPerBrowser =>
      sender() ! RequestsPerBrowser(
        values =
          receivedRequests.groupBy( _.browser ).mapValues( _.size )
      )
  }
}

object StatsAggregatorActor
{
  sealed trait Responses
  sealed trait Statistic extends Responses
  case class RequestsPerBrowser(values: Map[String, Int]) extends Statistic

  sealed trait Requests
  case object GetRequestsPerBrowser extends Requests
  
  def props() = Props(new StatsAggregatorActor)

  case object ForceFailure
  case class ForcedFailureException(msg: String = "") extends IllegalStateException(msg)
}