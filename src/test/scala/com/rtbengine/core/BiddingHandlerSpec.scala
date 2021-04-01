package com.rtbengine.core

import scala.language.postfixOps

import akka.actor.{ ActorSystem, Props }

import akka.testkit.{ ImplicitSender, TestKit }

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class BiddingHandlerSpec  extends TestKit(ActorSystem("MyTestSystem"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def beforeAll {
    Thread.sleep(1000)
  }

  override def afterAll {
    Thread.sleep(1000)
    TestKit.shutdownActorSystem(system)
  }

  import BiddingHandler._

  val testSiteIdOne     = "testSite1"
  val testSiteIdTwo     = "testSite2"
  val testSiteIdThree   = "testSite3"
  val testBidResponseId = "testResponseId"
  val bidRequestId      = "test123"

  val biddingHandler = system.actorOf(Props(new BiddingHandler {
    override def setBidResponseId: String = testBidResponseId

    override def setCampaigns: Seq[Campaign] = List(
      Campaign(
        id        = 1,
        country   = "SA",
        targeting = Targeting(
          targetedSiteIds = List(
            testSiteIdOne,
            testSiteIdTwo
          )
        ),
        banners   = List(
          Banner(id = 1, src = "testStuff1", width = 20, height = 20),
          Banner(id = 2, src = "testStuff2", width = 20, height = 25),
          Banner(id = 3, src = "testStuff3", width = 25, height = 25)
        ),
        bid       = 5d
      ),
      Campaign(
        id        = 2,
        country   = "NG",
        targeting = Targeting(
          targetedSiteIds = List(
            testSiteIdOne,
            testSiteIdThree
          )
        ),
        banners   = List(
          Banner(id = 1, src = "testStuff1", width = 20, height = 20),
          Banner(id = 2, src = "testStuff2", width = 20, height = 25),
          Banner(id = 3, src = "testStuff3", width = 25, height = 25)
        ),
        bid       = 3d
      )
    )
  }))

  "The BiddingHandler" must {
    "process a BidRequest with valid siteId but no device or user geo country, and Impression, and return None" in {
      biddingHandler ! BidRequest(
        id     = "test123",
        imp    = None,
        site   = Site(
          id     = testSiteIdOne,
          domain = "testSite.xyz"
        ),
        user   = None,
        device = None
      )
      expectMsg(None)
    }

    "process a BidRequest with an invalid siteId and return None" in {
      biddingHandler ! BidRequest(
        id     = "test123",
        imp    = None,
        site   = Site(
          id     = "invalidSite123",
          domain = "testSite.xyz"
        ),
        user   = None,
        device = None
      )
      expectMsg(None)

      biddingHandler ! BidRequest(
        id     = bidRequestId,
        imp    = Some(List(Impression(
          id       = "testImpId",
          wmin     = None,
          wmax     = None,
          w        = Some(20),
          hmin     = None,
          hmax     = None,
          h        = Some(20),
          bidFloor = Some(4.0)
        ))),
        site   = Site(
          id     = "invalidSiteId",
          domain = "testSite.xyz"
        ),
        user   = Some(User(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("NG") ))
        )),
        device = Some(Device(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("SA") ))
        ))
      )
      expectMsg(None)
    }

    "process a validated BidRequest and return a BidResponse only when the bidFloor is surpassed" in {
      biddingHandler ! BidRequest(
        id     = bidRequestId,
        imp    = Some(List(Impression(
          id       = "testImpId",
          wmin     = None,
          wmax     = None,
          w        = Some(20),
          hmin     = None,
          hmax     = None,
          h        = Some(20),
          bidFloor = Some(20.0)
        ))),
        site   = Site(
          id     = testSiteIdOne,
          domain = "testSite.xyz"
        ),
        user   = Some(User(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("NG") ))
        )),
        device = Some(Device(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("SA") ))
        ))
      )
      expectMsg(None)

      biddingHandler ! BidRequest(
        id     = bidRequestId,
        imp    = Some(List(Impression(
          id       = "testImpId",
          wmin     = None,
          wmax     = None,
          w        = Some(20),
          hmin     = None,
          hmax     = None,
          h        = Some(20),
          bidFloor = Some(4.0)
        ))),
        site   = Site(
          id     = testSiteIdOne,
          domain = "testSite.xyz"
        ),
        user   = Some(User(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("NG") ))
        )),
        device = Some(Device(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("SA") ))
        ))
      )
      expectMsg(Some(BidResponse(
        id = testBidResponseId,
        bidRequestId = bidRequestId,
        price = 5d,
        adid = Some("1"),
        banner = Some(Banner(
          id = 1,
          src = "testStuff1",
          width = 20,
          height = 20
        ))
      )))
    }

    "process a validated BidRequest with either device geo country or user geo country but prioritize device.geo and return a BidResponse" in {
      biddingHandler ! BidRequest(
        id     = bidRequestId,
        imp    = Some(List(Impression(
          id       = "testImpId",
          wmin     = None,
          wmax     = None,
          w        = Some(20),
          hmin     = None,
          hmax     = None,
          h        = Some(20),
          bidFloor = Some(4.0)
        ))),
        site   = Site(
          id     = testSiteIdOne,
          domain = "testSite.xyz"
        ),
        user   = Some(User(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("NG") ))
        )),
        device = Some(Device(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("SA") ))
        ))
      )
      expectMsg(Some(BidResponse(
        id = testBidResponseId,
        bidRequestId = bidRequestId,
        price = 5d,
        adid = Some("1"),
        banner = Some(Banner(
          id = 1,
          src = "testStuff1",
          width = 20,
          height = 20
        ))
      )))

      biddingHandler ! BidRequest(
        id     = bidRequestId,
        imp    = Some(List(Impression(
          id       = "testImpId",
          wmin     = None,
          wmax     = None,
          w        = Some(20),
          hmin     = None,
          hmax     = None,
          h        = Some(20),
          bidFloor = Some(1.0)
        ))),
        site   = Site(
          id     = testSiteIdOne,
          domain = "testSite.xyz"
        ),
        user   = Some(User(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("NG") ))
        )),
        device = None
      )
      expectMsg(Some(BidResponse(
        id = testBidResponseId,
        bidRequestId = bidRequestId,
        price = 3d,
        adid = Some("2"),
        banner = Some(Banner(
          id = 1,
          src = "testStuff1",
          width = 20,
          height = 20
        ))
      )))
    }

    "process a validated BidRequest with banner dimensions but prioritize h and w fields and return a BidResponse" in {
      biddingHandler ! BidRequest(
        id     = bidRequestId,
        imp    = Some(List(Impression(
          id       = "testImpId",
          wmin     = Some(150),
          wmax     = Some(200),
          w        = Some(20),
          hmin     = Some(120),
          hmax     = Some(250),
          h        = Some(25),
          bidFloor = Some(4.0)
        ))),
        site   = Site(
          id     = testSiteIdOne,
          domain = "testSite.xyz"
        ),
        user   = Some(User(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("NG") ))
        )),
        device = Some(Device(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("SA") ))
        ))
      )
      expectMsg(Some(BidResponse(
        id = testBidResponseId,
        bidRequestId = bidRequestId,
        price = 5d,
        adid = Some("1"),
        banner = Some(Banner(
          id = 2,
          src = "testStuff2",
          width = 20,
          height = 25
        ))
      )))
    }

    "process banner dimensions without h and w fields, but different combinations of min/max values then return a BidResponse" in {
      biddingHandler ! BidRequest(
        id     = bidRequestId,
        imp    = Some(List(Impression(
          id       = "testImpId",
          wmin     = Some(23),
          wmax     = None,
          w        = None,
          hmin     = Some(23),
          hmax     = None,
          h        = None,
          bidFloor = Some(4.0)
        ))),
        site   = Site(
          id     = testSiteIdOne,
          domain = "testSite.xyz"
        ),
        user   = Some(User(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("NG") ))
        )),
        device = Some(Device(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("SA") ))
        ))
      )
      expectMsg(Some(BidResponse(
        id = testBidResponseId,
        bidRequestId = bidRequestId,
        price = 5d,
        adid = Some("1"),
        banner = Some(Banner(
          id = 3,
          src = "testStuff3",
          width = 25,
          height = 25
        ))
      )))

      biddingHandler ! BidRequest(
        id     = bidRequestId,
        imp    = Some(List(Impression(
          id       = "testImpId",
          wmin     = None,
          wmax     = Some(21),
          w        = None,
          hmin     = None,
          hmax     = Some(21),
          h        = None,
          bidFloor = Some(4.0)
        ))),
        site   = Site(
          id     = testSiteIdOne,
          domain = "testSite.xyz"
        ),
        user   = Some(User(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("NG") ))
        )),
        device = Some(Device(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("SA") ))
        ))
      )
      expectMsg(Some(BidResponse(
        id = testBidResponseId,
        bidRequestId = bidRequestId,
        price = 5d,
        adid = Some("1"),
        banner = Some(Banner(
          id = 1,
          src = "testStuff1",
          width = 20,
          height = 20
        ))
      )))

      biddingHandler ! BidRequest(
        id     = bidRequestId,
        imp    = Some(List(Impression(
          id       = "testImpId",
          wmin     = Some(22),
          wmax     = None,
          w        = None,
          hmin     = None,
          hmax     = Some(26),
          h        = None,
          bidFloor = Some(4.0)
        ))),
        site   = Site(
          id     = testSiteIdOne,
          domain = "testSite.xyz"
        ),
        user   = Some(User(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("NG") ))
        )),
        device = Some(Device(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("SA") ))
        ))
      )
      expectMsg(Some(BidResponse(
        id = testBidResponseId,
        bidRequestId = bidRequestId,
        price = 5d,
        adid = Some("1"),
        banner = Some(Banner(
          id = 3,
          src = "testStuff3",
          width = 25,
          height = 25
        ))
      )))

      biddingHandler ! BidRequest(
        id     = bidRequestId,
        imp    = Some(List(Impression(
          id       = "testImpId",
          wmin     = None,
          wmax     = Some(22),
          w        = None,
          hmin     = Some(22),
          hmax     = None,
          h        = None,
          bidFloor = Some(4.0)
        ))),
        site   = Site(
          id     = testSiteIdOne,
          domain = "testSite.xyz"
        ),
        user   = Some(User(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("NG") ))
        )),
        device = Some(Device(
          id = "testDeviceId",
          geo = Some(Geo( country = Some("SA") ))
        ))
      )
      expectMsg(Some(BidResponse(
        id = testBidResponseId,
        bidRequestId = bidRequestId,
        price = 5d,
        adid = Some("1"),
        banner = Some(Banner(
          id = 2,
          src = "testStuff2",
          width = 20,
          height = 25
        ))
      )))
    }
  }

}
