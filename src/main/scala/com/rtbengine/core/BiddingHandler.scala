package com.rtbengine.core

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }
import scala.util.Random

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
  case class Site(id: String, domain: String)
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
  case class Campaign(
    id: Int,
    country: String,
    targeting: Targeting,
    banners: List[Banner],
    bid: Double
  )

  type siteId = String
  case class Targeting(targetedSiteIds: Seq[siteId])
}

class BiddingHandler extends Actor with ActorLogging with CampaignCacheT {

  import BiddingHandler._

  override def receive = {
    case req: BidRequest =>
      log.info("processing " + req)
      val currentSender = sender
      val reqSiteId     = req.site.id

      siteIdMap.get(reqSiteId) match {
        case Some(campaignList) =>
          val deviceGeoFut = for {
            device  <- req.device
            geo     <- device.geo
            country <- geo.country
          } yield country

          deviceGeoFut match {
            case Some(country) =>
              getCamp(req, campaignList, country) onComplete {
                case Success(response) =>
                  response match {
                    case Some(res) =>
                      currentSender ! Some(BidResponse(
                        id           = "randomId123",
                        bidRequestId = req.id,
                        price        = res._1.bid,
                        adid         = Some(res._1.id.toString),
                        banner       = Some(res._2)
                      ))
                    case None      =>
                      log.error(s"No Campaigns were found for request: [$req]")
                      currentSender ! None
                  }
                case Failure(error)    =>
                  log.error(s"An error occurred while processing request: [$req]" + Some(error))
                  currentSender ! None
              }
            case None          =>
              val userGeoFut = for {
                user    <- req.user
                geo     <- user.geo
                country <- geo.country
              } yield country

              userGeoFut match {
                case Some(country) =>
                  getCamp(req, campaignList, country) onComplete {
                    case Success(response) =>
                      response match {
                        case Some(res) =>
                          currentSender ! Some(BidResponse(
                            id           = "randomId123",
                            bidRequestId = req.id,
                            price        = res._1.bid,
                            adid         = Some(res._1.id.toString),
                            banner       = Some(res._2)
                          ))
                        case None      =>
                          log.error(s"No Campaigns were found for request: [$req]")
                          currentSender ! None
                      }
                    case Failure(error)    =>
                      log.error(s"An error occurred while processing request: [$req]" + Some(error))
                      currentSender ! None
                  }
                case None          =>
                  log.error(s"No Campaigns were found for request, the request does not include a device.geo or user.geo country: [$req]")
                  currentSender ! None
              }
          }

        case None     =>
          log.error(s"No Campaigns were found for siteId: [$reqSiteId]" + req)
          currentSender ! None
      }
  }

  private def getCamp(req: BidRequest, campaignList: List[Campaign], country: String): Future[Option[(Campaign, Banner)]] = Future {
    val campForCountry = campaignList.filter(_.country == country)
    req.imp match {
      case Some(imps) =>
        val validResponsesForDimensions: List[(Option[Campaign], Option[Banner])] = for {
          imp         <- imps
          camp        <- campForCountry
          bannerInfo  <- camp.banners
          impBidFloor <- imp.bidFloor
          if camp.bid >= impBidFloor
        } yield {
          (imp.w, imp.wmin, imp.wmax, imp.h, imp.hmin, imp.hmax) match {
            case (Some(w), _, _, Some(h), _, _)       => if(w == bannerInfo.width && h == bannerInfo.height) (Some(camp), Some(bannerInfo)) else (None, None)

            case (_, Some(wmin), _, _, Some(hmin), _) => if(bannerInfo.width >= wmin && bannerInfo.height >= hmin) (Some(camp), Some(bannerInfo)) else (None, None)

            case (_, _, Some(wmax), _, _, Some(hmax)) => if(bannerInfo.width <= wmax && bannerInfo.height <= hmax) (Some(camp), Some(bannerInfo)) else (None, None)

            case (_, Some(wmin), _, _, _, Some(hmax)) => if(bannerInfo.width >= wmin && bannerInfo.height <= hmax) (Some(camp), Some(bannerInfo)) else (None, None)

            case (_, _, Some(wmax), _, Some(hmin), _) => if(bannerInfo.width <= wmax && bannerInfo.height >= hmin) (Some(camp), Some(bannerInfo)) else (None, None)

            case _ => (None, None)
          }
        }
        val validResponses: List[(Campaign, Banner)] = for {
          validRes   <- validResponsesForDimensions
          getCamps   <- validRes._1
          getBanners <- validRes._2
        } yield (getCamps, getBanners)

        val validRes: Option[(Campaign, Banner)] = if(validResponses.isEmpty) None else Some(validResponses(
          Random.between(0, validResponses.length)
        ))

        validRes
      case None       =>
        log.error(s"No Campaigns were found for request, the request does not contain an Impression: [$req]")
        None
    }
  }
}
