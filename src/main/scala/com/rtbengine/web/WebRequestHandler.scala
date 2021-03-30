package com.rtbengine.web

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.ask
import akka.util.Timeout

import com.rtbengine._

import core.BiddingHandler._

class WebRequestHandler extends Actor with ActorLogging {

  implicit val timeout   = Timeout(20 seconds)

  override def receive = {
    case req: BidRequest =>
      log.info("processing " + req)
  }

}
