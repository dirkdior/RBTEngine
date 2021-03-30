package com.rtbengine.web

import akka.http.scaladsl.marshallers.sprayjson._
import spray.json._

import com.rtbengine._

import core.BiddingHandler._

private [web] object WebServiceMarshalling {

  trait WebServiceJsonSupportT extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val ImpressionJsonSupport  = jsonFormat8(Impression)
    implicit val SiteJsonSupport        = jsonFormat2(Site)
    implicit val GeoJsonSupport         = jsonFormat1(Geo)
    implicit val UserJsonSupport        = jsonFormat2(User)
    implicit val DeviceJsonSupport      = jsonFormat2(Device)
    implicit val BannerJsonSupport      = jsonFormat4(Banner)

    implicit val BidRequestJsonSupport  = jsonFormat5(BidRequest)
    implicit val BidResponseJsonSupport = jsonFormat5(BidResponse)
  }
}
