package com.rtbengine.core

import java.util.UUID._
import java.security.MessageDigest

import BiddingHandler._

private [core] trait CampaignCacheT {

  val siteIds: List[String] = List("41725c9f64d3bdb62ef2727559f45b04", "7662271e8466735500f91210c9a008a6", "d21b44cc9cd3ac64e61ad79024f9cd7d",
                                    "299c09204d370604ee319d9e3e7cfca4", "856ec07f708c471860b9ff9013a1aba9", "559c8650881c36d5c7281817c98752b5",
                                    "f09463c798f4543343d3a7ad36134a79", "a02ca6a4b621456ca6780203af95b888", "46f50cd7afc5b8adfa575b8db361819a",
                                    "747efc871b601ff343e2b861505dab04", "e321787c9da42d241614583e4f2c616b", "7b352dd8ed8f75068dee5a6528fc081a")

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

  def md5Hash(value: String): String = MessageDigest.getInstance("MD5").digest(value.getBytes).map("%02x".format(_)).mkString

  def setCampaigns: Seq[Campaign] = List(
    Campaign(
      id        = 101,
      country   = "LT",
      targeting = Targeting(
        targetedSiteIds = siteIds.slice(0, 10)
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
        targetedSiteIds = siteIds.slice(2, 12)
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
