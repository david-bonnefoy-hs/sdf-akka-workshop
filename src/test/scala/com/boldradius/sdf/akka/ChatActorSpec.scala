package com.boldradius.sdf.akka

import akka.testkit.EventFilter
import com.boldradius.sdf.akka.ChatActor.HelpRequested
import scala.concurrent.duration._

/**
 * Created by davidb on 15-06-26.
 */
class ChatActorSpec extends BaseAkkaSpec {

  "User staying on help page" should {
    "chat agent log request for help" in {
      val chatActor = system.actorOf(ChatActor.props)
      val tracker = system.actorOf(SessionTracker.props(123, sessionTimeout = 100 second, helpTimeout = 0 second, chatActor))
      EventFilter.info(pattern = ".*User 123 requested help.*", occurrences = 1) intercept {
        tracker ! Request(123, 0, "/help", "", "")
      }
    }
  }

}
