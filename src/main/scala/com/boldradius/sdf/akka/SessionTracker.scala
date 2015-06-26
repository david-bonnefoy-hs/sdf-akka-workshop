package com.boldradius.sdf.akka

import akka.actor._
import com.boldradius.sdf.akka.ChatActor.HelpRequested
import com.boldradius.sdf.akka.SessionTracker.{UserNeedsHelp, LastRequest}

import scala.concurrent.duration.FiniteDuration

/**
 * Created by davidb on 15-06-24.
 */
class SessionTracker(sessionId: Long, sessionTimeout: FiniteDuration, helpTimeout: FiniteDuration, chatActor: ActorRef)
  extends Actor with ActorLogging {

  import context.dispatcher

  var sessionTimeoutTimer = createSessionTimeoutTimer

  var helpTimeoutTimer: Cancellable = null
  var helpWasRequested: Boolean = false

  var lastRequest: Request = null

  override def receive: Receive = {
    case request: Request =>
      sessionTimeoutTimer.cancel()
      sessionTimeoutTimer = createSessionTimeoutTimer
      checkNeedForHelp(request)
      lastRequest = request

    case SessionTracker.Timeout =>
      log.info(s"User $sessionId is idle.")
      context.stop(self)

    case LastRequest =>
      sender() ! lastRequest

    case UserNeedsHelp =>
      if (!helpWasRequested) {
        chatActor ! HelpRequested(sessionId)
        helpWasRequested = true
      }
  }

  def createSessionTimeoutTimer: Cancellable = {
    context.system.scheduler.scheduleOnce (
      sessionTimeout,
      self,
      SessionTracker.Timeout
    )
  }

  def checkNeedForHelp(request: Request): Unit = {
    if (request.url.contains("/help")) {
      if (helpTimeoutTimer == null) {
        helpTimeoutTimer = createHelpTimeoutTimer
      }
    }
    else {
      if (helpTimeoutTimer != null) {
        helpTimeoutTimer.cancel()
        helpTimeoutTimer = null
      }
    }
  }

  def createHelpTimeoutTimer: Cancellable = {
    context.system.scheduler.scheduleOnce (
      helpTimeout,
      self,
      SessionTracker.UserNeedsHelp
    )
  }

}

object SessionTracker {

  def props(sessionId: Long, sessionTimeout: FiniteDuration, helpTimeout: FiniteDuration, chatActor: ActorRef) =
    Props(new SessionTracker(sessionId, sessionTimeout, helpTimeout, chatActor))

  case object Timeout
  case object LastRequest
  case object UserNeedsHelp
}