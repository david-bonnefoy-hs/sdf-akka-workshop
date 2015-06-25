package com.boldradius.sdf.akka

import akka.actor.ActorRef
import akka.testkit.TestProbe
import com.boldradius.sdf.akka.EmailActor.EmailMessage
import com.boldradius.sdf.akka.RealTimeStatAggregator.usersPerBrowser
import com.boldradius.sdf.akka.RequestConsumer.{GetRealTimeStatistics, FailAggregator}
import com.boldradius.sdf.akka.StatsAggregatorActor.ForceFailure

/**
 * Created by davidb on 15-06-25.
 */
class RequestConsumerSpec extends BaseAkkaSpec {

  "RequestConsumer" should {
    "send a message to email actor when stats aggregator fails for than max number of times" in {
      val emailActor = TestProbe()
      val consumer: ActorRef = system.actorOf(RequestConsumer.props(2, emailActor.ref))
      consumer ! FailAggregator
      consumer ! FailAggregator
      emailActor.expectMsg(EmailMessage("Aggregator agent failed"))
    }
  }

  "Real-time stat" should {
    "Reflect current session trackers" in {
      val emailActor = TestProbe()
      val requester = TestProbe()
      val consumer: ActorRef = system.actorOf(RequestConsumer.props(2, emailActor.ref))
      consumer ! Request(1, 1, "", "", "Opera")
      consumer ! Request(2, 1, "", "", "Maxthon")
      consumer.tell(GetRealTimeStatistics(usersPerBrowser), requester.ref)
      requester.expectMsg(Map("Opera" -> 1, "Maxthon" -> 1))
    }
  }

}
