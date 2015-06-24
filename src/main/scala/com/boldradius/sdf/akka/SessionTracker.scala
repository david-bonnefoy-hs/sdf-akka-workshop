package com.boldradius.sdf.akka

import akka.actor.{Actor, ActorLogging, Cancellable, Props}

import scala.concurrent.duration.FiniteDuration

/**
 * Created by davidb on 15-06-24.
 */
class SessionTracker(sessionId: Long, timeout: FiniteDuration) extends Actor with ActorLogging {

  import context.dispatcher

  var currentTimer = createTimer

  override def receive: Receive = {
    case request: Request =>
      currentTimer.cancel()
      currentTimer = createTimer
    case SessionTracker.Timeout =>
      log.info(s"User $sessionId is idle.")
  }

  def createTimer: Cancellable = {
    context.system.scheduler.scheduleOnce (
      timeout,
      self,
      SessionTracker.Timeout
    )
  }

}

object SessionTracker {

  def props(sessionId: Long, timeout: FiniteDuration) =
    Props(new SessionTracker(sessionId, timeout))

  case object Timeout
}