package com.boldradius.sdf.akka

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
  "StatsAggregator" should {
    "report browser usage statistics" in {

      implicit val timeout = Timeout(100 day)

      val stats: ActorRef = system.actorOf(StatsAggregatorActor.props())

      stats ! Request(1, 0, "microsoft.com", "referer1", "IE")
      stats ! Request(1, 0, "google.com", "referer1", "IE")
      stats ! Request(1, 0, "apple.com", "referer1", "IE")
      stats ! Request(1, 0, "lolcats.org", "referer1", "Opera")
      val future: Future[RequestsPerBrowser] = (stats ? GetRequestsPerBrowser).mapTo[RequestsPerBrowser]
      Await.result(future, atMost = 5 second) should be (RequestsPerBrowser(Map("IE" -> 3, "Opera" -> 1)))
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
