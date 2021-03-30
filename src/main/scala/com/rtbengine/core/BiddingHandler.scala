package com.rtbengine.core

import akka.actor.{ Actor, ActorLogging }

object BiddingHandler {
  case class BidRequest(
    id: String,
    imp: Option[List[Impression]],
    site: Site,
    user: Option[User],
    device: Option[Device]
  )
  case class Impression(
    id: String,
    wmin: Option[Int],
    wmax: Option[Int],
    w: Option[Int],
    hmin: Option[Int],
    hmax: Option[Int],
    h: Option[Int],
    bidFloor: Option[Double]
  )
  case class Site(id: Int, domain: String)
  case class User(id: String, geo: Option[Geo])
  case class Device(id: String, geo: Option[Geo])
  case class Geo(country: Option[String])

  case class BidResponse(
    id: String,
    bidRequestId: String,
    price: Double,
    adid: Option[String],
    banner: Option[Banner]
  )
  case class Banner(
    id: Int,
    src: String,
    width: Int,
    height: Int
  )
}

class BiddingHandler extends Actor with ActorLogging {

  import BiddingHandler._

  override def receive = {
    case req: BidRequest =>
      log.info("processing " + req)
  }

}
