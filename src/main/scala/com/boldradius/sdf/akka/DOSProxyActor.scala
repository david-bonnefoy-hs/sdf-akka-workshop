package com.boldradius.sdf.akka

import akka.actor.{Actor, ActorRef, Props}
import com.boldradius.sdf.akka.DOSProxyActor.Rejected

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

/**
 * Created by jonathann on 2015-06-25.
 */
class DOSProxyActor(worker: ActorRef) extends Actor
{

  val requestLog = new ListBuffer[Request]

  override def receive: Receive = {
    case request: Request =>
      val recentRequestCount = requestsInTheLast(1 second) size

      if (recentRequestCount <= 10)
      {
        worker forward request

        requestLog += request
      } else
      {
        sender() ! Rejected(request, recentRequestCount)
      }
  }

  private def requestsInTheLast(duration: FiniteDuration): List[Request] =
  {
    val list = requestLog.toList
    requestLog.lastOption map {
      lastRequest =>
        list.dropWhile {
          currentRequest =>
            val diff = (lastRequest.timestamp - currentRequest.timestamp) seconds

            diff > duration
        }
    } getOrElse Nil
  }
}

object DOSProxyActor
{
  case class Rejected(request: Request, exceeded: Int)
  def props(worker: ActorRef) = Props(new DOSProxyActor(worker))
}
