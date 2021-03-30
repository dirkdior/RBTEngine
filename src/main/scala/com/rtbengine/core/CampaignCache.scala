package com.rtbengine.core

import java.util.UUID._

import BiddingHandler._

trait CampaignCacheT {

  val siteIdOne   = randomUUID.toString
  val siteIdTwo   = randomUUID.toString
  val siteIdThree = randomUUID.toString

  var siteIds: Map[siteId, List[Campaign]] = Map.empty[siteId, List[Campaign]]
  val campaignCache: Seq[Campaign]         = getCampaigns

  def getCampaigns: Seq[Campaign] = List(
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


  for(campaign <- campaignCache) {
    for(siteId <- campaign.targeting.targetedSiteIds) {
      siteIds.get(siteId) match {
        case Some(campaignsForSiteId) =>
          siteIds += (siteId -> (campaign :: campaignsForSiteId))
        case None                     =>
          siteIds += (siteId -> List(campaign))
      }
    }
  }

  siteIds.foreach(println)

}
