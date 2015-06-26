package com.boldradius.sdf.akka

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.boldradius.sdf.akka.DOSProxyActor.Rejected

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by jonathann on 2015-06-25.
 */
class DOSProxyActorSpec extends BaseAkkaSpec
{
  implicit val timeout = Timeout(5 second)

  "a DOS proxy actor" should {
//    "pass all requests occuring no more than 10 per second " in {
//      val dos: ActorRef = system.actorOf(DOSProxyActor.props(system.deadLetters))
//
//      dos ! Request(1, 0, "google.com", "referer", "browser")
//      dos ! Request(1, 0, "google.com", "referer", "browser")
//      dos ! Request(1, 0, "google.com", "referer", "browser")
//      dos ! Request(1, 1, "microsoft.com", "referer", "browser")
//      dos ! Request(1, 1, "microsoft.com", "referer", "browser")
//      dos ! Request(1, 1, "microsoft.com", "referer", "browser")
//      dos ! Request(1, 2, "microsoft.com", "referer", "browser")
//      dos ! Request(1, 2, "microsoft.com", "referer", "browser")
//      dos ! Request(1, 2, "boldradius.com", "referer", "browser")
//      dos ! Request(1, 3, "boldradius.com", "referer", "browser")
//      dos ! Request(1, 4, "boldradius.com", "referer", "browser")
//      dos ! Request(1, 4, "boldradius.com", "referer", "browser")
//      dos ! Request(1, 5, "google.com", "referer", "browser")
//      dos ! Request(1, 6, "google.com", "referer", "browser")
//      dos ! Request(1, 7, "google.com", "referer", "browser")
//
//      val rejectRequest = Request(1, 0, "wikipedia.org", "referer", "browser")
//      Await.result(
//        (dos ? rejectRequest).mapTo[Rejected],
//        atMost = 5 second
//      ) should be (
//        Rejected(
//          rejectRequest,
//          11
//        )
//      )
//    }

    "reject DOS attacks " in {
      val dos: ActorRef = system.actorOf(DOSProxyActor.props(system.deadLetters))

      dos ! Request(1, 0, "google.com", "referer", "browser")
      dos ! Request(1, 0, "google.com", "referer", "browser")
      dos ! Request(1, 0, "google.com", "referer", "browser")
      dos ! Request(1, 1, "microsoft.com", "referer", "browser")
      dos ! Request(1, 1, "microsoft.com", "referer", "browser")
      dos ! Request(1, 1, "microsoft.com", "referer", "browser")
      dos ! Request(1, 1, "microsoft.com", "referer", "browser")
      dos ! Request(1, 1, "microsoft.com", "referer", "browser")
      dos ! Request(1, 1, "boldradius.com", "referer", "browser")
      dos ! Request(1, 1, "boldradius.com", "referer", "browser")
      dos ! Request(1, 1, "boldradius.com", "referer", "browser")
      dos ! Request(1, 1, "boldradius.com", "referer", "browser")
      dos ! Request(1, 1, "google.com", "referer", "browser")
      dos ! Request(1, 1, "google.com", "referer", "browser")
      dos ! Request(1, 1, "google.com", "referer", "browser")

      val rejectRequest = Request(1, 0, "wikipedia.org", "referer", "browser")
      Await.result(
        (dos ? rejectRequest).mapTo[Rejected],
        atMost = 5 second
      ) should be (
        Rejected(
          rejectRequest,
          11
        )
      )
    }
  }

}
