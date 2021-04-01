package com.rtbengine.core

import java.util.UUID._
import java.security.MessageDigest

import BiddingHandler._

private [core] trait CampaignCacheT {

  val siteIdOne   = md5Hash(randomUUID.toString)
  val siteIdTwo   = md5Hash(randomUUID.toString)
  val siteIdThree = md5Hash(randomUUID.toString)

  var siteIdMap: Map[siteId, List[Campaign]] = Map.empty[siteId, List[Campaign]]
  val campaignCache: Seq[Campaign]           = setCampaigns

  for {
    campaign <- campaignCache
    siteId   <- campaign.targeting.targetedSiteIds
  } yield siteIdMap.get(siteId) match {
      case Some(campaignsForSiteId) =>
        siteIdMap += (siteId -> (campaign :: campaignsForSiteId))
      case None                     =>
        siteIdMap += (siteId -> List(campaign))
    }

  siteIdMap.foreach(println)

  def md5Hash(value: String): String = MessageDigest.getInstance("MD5").digest(value.getBytes).map("%02x".format(_)).mkString

  def setCampaigns: Seq[Campaign] = List(
    Campaign(
      id        = 101,
      country   = "LT",
      targeting = Targeting(
        targetedSiteIds = List(
          siteIdOne,
          siteIdTwo
        )
      ),
      banners   = List(
        Banner(id = 1, src = "stuff1", width = 21, height = 21),
        Banner(id = 2, src = "stuff2", width = 21, height = 25),
        Banner(id = 3, src = "stuff3", width = 25, height = 21)
      ),
      bid       = 5d
    ),
    Campaign(
      id        = 201,
      country   = "NG",
      targeting = Targeting(
        targetedSiteIds = List(
          siteIdOne,
          siteIdThree
        )
      ),
      banners   = List(
        Banner(id = 1, src = "stuff1", width = 21, height = 21),
        Banner(id = 2, src = "stuff2", width = 21, height = 25),
        Banner(id = 3, src = "stuff3", width = 25, height = 21)
      ),
      bid       = 5d
    )
  )
}
