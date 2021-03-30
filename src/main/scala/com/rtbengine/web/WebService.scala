package com.rtbengine.web

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import akka.actor.{ ActorRefFactory, Props }
import akka.event.Logging
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.DebuggingDirectives.logRequestResult
import akka.pattern.ask
import akka.util.Timeout

import com.rtbengine._

import core.BiddingHandler._

import WebServiceMarshalling.WebServiceJsonSupportT

trait WebServiceT extends WebServiceJsonSupportT {

  def actorRefFactory: ActorRefFactory

  implicit val timeout: Timeout = 10.seconds
  private val requestHandler    = actorRefFactory.actorOf(Props[WebRequestHandler])

  lazy val route = {
    path("status") {
      complete {
        StatusCodes.OK
      }
    } ~
      path("bid" / "create") {
        logRequestResult("bid:create", Logging.InfoLevel) {
          post {
            entity(as[BidRequest]) { request =>
              complete {
                (requestHandler ? request).mapTo[BidResponse] map { response =>
                  response
                }
              }
            }
          }
        }
      }
  }
}
