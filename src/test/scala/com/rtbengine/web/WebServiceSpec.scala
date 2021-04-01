package com.rtbengine.web

import scala.concurrent.duration._

import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import akka.http.scaladsl.model._
import akka.util.ByteString

import StatusCodes._

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import spray.json._

import com.rtbengine._

import core.BiddingHandler._

import WebServiceMarshalling._

class WebServiceSpec extends AnyWordSpec
  with Matchers
  with ScalatestRouteTest
  with WebServiceT
  with WebServiceJsonSupportT {

  def actorRefFactory           = system
  implicit val routeTestTimeout = RouteTestTimeout(FiniteDuration(20, "seconds"))

  "The RBT Engine WebService" should {
    "return an error 204 No Content for an invalid BidRequest" in {
      val bidRequestJson = BidRequest(
        id     = "test123",
        imp    = None,
        site   = Site(
          id     = "site123",
          domain = "testSite.xyz"
        ),
        user   = None,
        device = None
      ).toJson.toString

      HttpRequest(
        method = HttpMethods.POST,
        uri    = "/bid/create",
        entity = HttpEntity(ContentTypes.`application/json`, ByteString(bidRequestJson))
      ) ~> Route.seal(route) ~> check {
        status shouldEqual NoContent
      }
    }
  }
}
