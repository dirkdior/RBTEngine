package com.rtbengine.web

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.ask
import akka.util.Timeout

import com.rtbengine._

import core.BiddingHandler
import core.BiddingHandler._

class WebRequestHandler extends Actor with ActorLogging {

  implicit val timeout       = Timeout(20 seconds)
  private val biddingHandler = context.actorOf(Props[BiddingHandler])

  override def receive = {
    case req: BidRequest =>
      log.info("processing " + req)
      val currentSender     = sender

      val biddingHandlerFut = (biddingHandler ? req).mapTo[BidResponse]

      biddingHandlerFut onComplete {
        case Success(response) =>
          currentSender ! response
        case Failure(error)    =>
          log.error(s"Error from biddingHandler while processing [$req]" + Some(error))
      }
  }

}
