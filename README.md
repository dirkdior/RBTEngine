# Real time bidding engine

## Paths
To submit a bid, send a POST request to `http://127.0.0.1:8080/bid/create` once the has started running

## How to validate the solution
Include any of the siteIds from the list `val siteIds: List[String]` in ` trait CampaignCacheT`

Include a device.geo or user.geo country

Include at least one `Impression`

Include a `w` or `h` in the `Impression`, or any max or min combination

Check that the bidFloor in the `BidRequest` is lower than that of the `Campaign`

## Samples
Based on the Campaigns and SiteIds in `CampaignCacheT`

Here is a sample valid `BidRequest` that would return a `BidResponse`
```
{
  "id": "SGu1Jpq1IO",
  "site": {
	"id": "747efc871b601ff343e2b861505dab04",
	"domain": "fake.tld"
  },
  "device": {
	"id": "440579f4b408831516ebd02f6e1c31b4",
	"geo": {
  	    "country": "LT"
	}
  },
  "imp": [
	{
        "id": "1",
        "wmin": 50,
        "wmax": 300,
        "hmin": 100,
        "hmax": 300,
        "h": 21,
        "w": 27,
        "bidFloor": 3.12123
	},
    {
        "id": "2",
        "wmin": 50,
        "wmax": 300,
        "hmin": 100,
        "hmax": 300,
        "h": 21,
        "w": 25,
        "bidFloor": 3.12123
    }
  ],
  "user": {
	"geo": {
  	    "country": "LT"
	},
	"id": "USARIO1"
  }
}
```

The `BidResponse`:
```
{
    "adid": "101",
    "banner": {
        "height": 21,
        "id": 3,
        "src": "stuff3",
        "width": 25
    },
    "bidRequestId": "SGu1Jpq1IO",
    "id": "someRandomId",
    "price": 5.0
}
```