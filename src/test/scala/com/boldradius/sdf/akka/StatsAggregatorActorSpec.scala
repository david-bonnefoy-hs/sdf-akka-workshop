package com.boldradius.sdf.akka

import java.time.{LocalDateTime, ZoneOffset}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.testkit.EventFilter
import akka.util.Timeout
import com.boldradius.sdf.akka.StatsAggregatorActor._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
 * Created by jonathann on 2015-06-24.
 */
class StatsAggregatorActorSpec extends BaseAkkaSpec
{
  implicit val timeout = Timeout(5 second)

  private def dayPeriod(year: Int, month: Int, day: Int)(hour: Int, minute: Int): Long =
    LocalDateTime.of(year, month, day, hour, minute).toEpochSecond(ZoneOffset.UTC)


  "StatsAggregator" should {
    "report browser usage statistics" in {

      val stats: ActorRef = system.actorOf(StatsAggregatorActor.props())

      stats ! Request(1, 0, "microsoft.com", "referer1", "IE")
      stats ! Request(1, 0, "google.com", "referer1", "IE")
      stats ! Request(1, 0, "apple.com", "referer1", "IE")
      stats ! Request(1, 0, "lolcats.org", "referer1", "Opera")
      val future: Future[RequestsPerBrowser] = (stats ? GetRequestsPerBrowser).mapTo[RequestsPerBrowser]
      Await.result(future, atMost = 5 second) should be (RequestsPerBrowser(Map("IE" -> 3, "Opera" -> 1)))
    }

    "report business minute of day" in {
      val stats: ActorRef = system.actorOf(StatsAggregatorActor.props())

      val time = dayPeriod(2015, 1, 14) _

      stats ! Request(1, time(12, 5), "url", "referer", "browser")
      stats ! Request(1, time(11, 5), "url", "referer", "browser")
      stats ! Request(1, time(12, 6), "url", "referer", "browser")
      stats ! Request(1, time(12, 5), "url", "referer", "browser")
      stats ! Request(1, time(10, 7), "url", "referer", "browser")

      val future: Future[BusiestMinuteOfDay] = (stats ? GetBusiestMinuteOfDay).mapTo[BusiestMinuteOfDay]
      Await.result(future, atMost = 5 second) should be (BusiestMinuteOfDay(hour = 12, minute = 5, requests = 2))
    }

    "report URL distribution" in {
      val stats: ActorRef = system.actorOf(StatsAggregatorActor.props())

      stats ! Request(1, 0, "google.com", "referer", "browser")
      stats ! Request(1, 0, "microsoft.com", "referer", "browser")
      stats ! Request(1, 0, "boldradius.com", "referer", "browser")
      stats ! Request(2, 0, "hootsuite.com", "referer", "browser")
      stats ! Request(2, 0, "google.com", "referer", "browser")
      stats ! Request(2, 0, "google.com", "referer", "browser")
      stats ! Request(3, 0, "microsoft.com", "referer", "browser")
      stats ! Request(3, 0, "google.com", "referer", "browser")
      stats ! Request(3, 0, "hootsuite.com", "referer", "browser")
      stats ! Request(4, 0, "google.com", "referer", "browser")

      val future: Future[URLDistribution] = (stats ? GetURLDistribution).mapTo[URLDistribution]
      Await.result(future, atMost = 5 second) should be (
        URLDistribution(
          Map(
            "google.com" -> 0.5f,
            "microsoft.com" -> 0.2f,
            "hootsuite.com" -> 0.2f,
            "boldradius.com" -> 0.1f
          )
        )
      )
    }

    "report average page visit times" in {
      val time = dayPeriod(2015, 1, 14) _
      val stats: ActorRef = system.actorOf(StatsAggregatorActor.props())

      stats ! Request(1, time(1, 5), "google.com", "referer", "browser")
      stats ! Request(1, time(1, 15), "microsoft.com", "referer", "browser")
      stats ! Request(1, time(4, 12), "microsoft.com", "referer", "browser")
      stats ! Request(1, time(8, 15), "boldradius.com", "referer", "browser")
      stats ! Request(1, time(8, 16), "boldradius.com", "referer", "browser")
      stats ! Request(1, time(8, 20), "boldradius.com", "referer", "browser")
      stats ! Request(1, time(8, 24), "google.com", "referer", "browser")
      stats ! Request(1, time(8, 25), "boldradius.com", "referer", "browser")
      stats ! Request(1, time(8, 29), "google.com", "referer", "browser")
      stats ! Request(1, time(8, 32), "microsoft.com", "referer", "browser")
      stats ! Request(1, time(11, 40), "hootsuite.com", "referer", "browser")
      stats ! Request(1, time(11, 45), "hootsuite.com", "referer", "browser")
      stats ! Request(1, time(11, 55), "microsoft.com", "referer", "browser")
      stats ! Request(1, time(12, 0), "google.com", "referer", "browser")
      stats ! Request(2, time(1, 0), "google.com", "referer", "browser")
      stats ! Request(2, time(1, 17), "microsoft.com", "referer", "browser")
      stats ! Request(2, time(4, 11), "microsoft.com", "referer", "browser")
      stats ! Request(2, time(8, 16), "boldradius.com", "referer", "browser")
      stats ! Request(2, time(8, 18), "boldradius.com", "referer", "browser")
      stats ! Request(2, time(8, 22), "boldradius.com", "referer", "browser")
      stats ! Request(2, time(8, 25), "google.com", "referer", "browser")
      stats ! Request(2, time(8, 26), "boldradius.com", "referer", "browser")
      stats ! Request(2, time(8, 34), "google.com", "referer", "browser")
      stats ! Request(2, time(8, 48), "microsoft.com", "referer", "browser")
      stats ! Request(2, time(11, 40), "hootsuite.com", "referer", "browser")
      stats ! Request(2, time(11, 45), "hootsuite.com", "referer", "browser")
      stats ! Request(2, time(11, 55), "microsoft.com", "referer", "browser")
      stats ! Request(2, time(12, 0), "google.com", "referer", "browser")

      val future: Future[AverageURLVisitTime] = (stats ? GetAverageURLVisitTime).mapTo[AverageURLVisitTime]
      Await.result(future, atMost = 5 second) should be (
        AverageURLVisitTime(
          Map(
            "google.com" -> (5.minutes + 50.seconds),
            "microsoft.com" -> (2.hours + 31.minutes + 7.seconds + 500.millis),
            "hootsuite.com" -> (7.minutes + 30.seconds),
            "boldradius.com" -> (3.minutes + 45.seconds)
          )
        )
      )
    }
  }

  "StatsAggregator" should {
    "fail with ForcedFailureException" in {
      val stats: ActorRef = system.actorOf(StatsAggregatorActor.props())
      EventFilter[ForcedFailureException](occurrences = 1) intercept {
        stats ! ForceFailure
      }
    }
  }
}
