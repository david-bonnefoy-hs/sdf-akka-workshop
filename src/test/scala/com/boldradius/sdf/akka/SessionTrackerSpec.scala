package com.boldradius.sdf.akka

/**
 * Created by davidb on 15-06-24.
 */

import java.util.concurrent.TimeUnit
import akka.testkit.{ EventFilter, TestProbe }
import com.boldradius.sdf.akka.ChatActor.HelpRequested
import scala.concurrent.duration._

class SessionTrackerSpec extends BaseAkkaSpec {

  "Creating SessionTracker with timeout of 0" should {
    "result in logging a session idle message" in {
      EventFilter.info(pattern = ".*is idle.*", occurrences = 1) intercept {
        system.actorOf(SessionTracker.props(0, 0 second, 0 second, null))
      }
    }
  }

  "User staying on help page" should {
    "request help to chat agent" in {
      val chatActor = TestProbe()
      val tracker = system.actorOf(SessionTracker.props(123, sessionTimeout = 100 second, helpTimeout = 0 second, chatActor.ref))
      tracker ! Request(123, 0, "/help", "", "")
      chatActor.expectMsg(HelpRequested(123))
    }
  }

}
