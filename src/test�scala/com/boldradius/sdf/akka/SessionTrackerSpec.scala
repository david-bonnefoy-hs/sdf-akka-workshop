package com.boldradius.sdf.akka

/**
 * Created by davidb on 15-06-24.
 */

import java.util.concurrent.TimeUnit
import akka.testkit.{ EventFilter, TestProbe }
import scala.concurrent.duration._

class SessionTrackerSpec extends BaseAkkaSpec {

  "Creating SessionTracker with timeout of 0" should {
    "result in logging a session idle message" in {
      EventFilter.info(pattern = ".*is idle.*", occurrences = 1) intercept {
        system.actorOf(SessionTracker.props(0, 0 second))
      }
    }
  }

}
