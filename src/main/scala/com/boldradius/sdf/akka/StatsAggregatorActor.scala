package com.boldradius.sdf.akka

import java.time._

import akka.actor.{Actor, Props}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

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

    case GetBusiestMinuteOfDay =>
      implicit val requestCountOrdering = new Ordering[BusiestMinuteOfDay]
      {
        override def compare(x: BusiestMinuteOfDay, y: BusiestMinuteOfDay): Int = x.requests - y.requests
      }

      val ((hour, minute), requests) =
        receivedRequests.groupBy {
          x =>
            val time = LocalDateTime.ofEpochSecond(x.timestamp, 0, ZoneOffset.UTC)

            (time.getHour, time.getMinute)
        } mapValues ( _.size ) maxBy (_._2)

      sender() ! BusiestMinuteOfDay(hour, minute, requests)

    case GetURLDistribution =>
      val denominator = receivedRequests.size.toFloat
      val result = receivedRequests.groupBy ( _.url ) mapValues ( _.size.toFloat / denominator )

      sender() ! URLDistribution(result)

    case GetAverageURLVisitTime =>

      def pageVisitTimes (requests: List[Request]): List[(String, FiniteDuration)] =
      {
        val sliders: Iterator[List[Request]] = requests.sortBy(_.timestamp).sliding(2)
        val leading =
          sliders map {
            case left :: right :: Nil =>
              (left.url, (right.timestamp - left.timestamp) seconds)
          } toList

        val maybeInfiniteLast = requests.lastOption map {
          request =>
            (request.url, 20 seconds)
        }

        leading ::: maybeInfiniteLast.toList
      }

      def averageTimes (completeUrlVisitTimes: List[(String, FiniteDuration)]): Map[String, FiniteDuration] =
      {
        def average (values: List[FiniteDuration]): FiniteDuration = {
          val sum =
            (values fold (0 seconds)) { (left, right) => left + right }

          sum / values.size
        }

        completeUrlVisitTimes groupBy ( _._1 ) mapValues {
          sessionUrlVisitTimes =>
            average(sessionUrlVisitTimes map (_._2))
        }
      }

      sender() ! AverageURLVisitTime(averageTimes(requestsBySession flatMap pageVisitTimes toList))
  }

  def requestsBySession: Traversable[List[Request]] =
    receivedRequests.toList.groupBy( _.sessionId ).values
}

object StatsAggregatorActor
{
  sealed trait Responses
  sealed trait Statistic extends Responses
  case class RequestsPerBrowser(values: Map[String, Int]) extends Statistic
  case class BusiestMinuteOfDay(hour: Int, minute: Int, requests: Int) extends Statistic
  case class URLDistribution(values: Map[String, Float]) extends Statistic
  case class AverageURLVisitTime(values: Map[String, FiniteDuration]) extends Statistic

  sealed trait Requests
  case object GetRequestsPerBrowser extends Requests
  case object GetBusiestMinuteOfDay extends Requests
  case object GetURLDistribution extends Requests
  case object GetAverageURLVisitTime extends Requests
  
  def props() = Props(new StatsAggregatorActor)

  case object ForceFailure
  case class ForcedFailureException(msg: String = "") extends IllegalStateException(msg)
}