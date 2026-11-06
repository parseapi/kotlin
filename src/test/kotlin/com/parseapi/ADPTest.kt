package com.parseapi
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking

class ADPTest {
 @Test fun modelsDecodeTriadThroughTheirOperations() = runBlocking {
  run {
   val plainStub = StubTransport(200, """
{"country":"US","name":"United States","continent":"NA"}
""")
   val plain = ParseAPI("test") { transport = plainStub }.country("US")
   val stub0 = StubTransport(200, """
{"country":"US","name":"United States","continent":"NA"}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.country("US") { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"country":"US","name":"United States","continent":"NA","iso3":"USA","population":0,"tax_rate":0,"plugs":[]}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.country("US") { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """
{"state":"NC","name":"North Carolina","country":"US"}
""")
   val plain = ParseAPI("test") { transport = plainStub }.state("NC")
   val stub0 = StubTransport(200, """
{"state":"NC","name":"North Carolina","country":"US"}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.state("NC") { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"state":"NC","name":"North Carolina","country":"US","population":0,"area":0.25,"tax_rate":0}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.state("NC") { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """
{"district":"37081","name":"Guilford","country":"US"}
""")
   val plain = ParseAPI("test") { transport = plainStub }.district("37081")
   val stub0 = StubTransport(200, """
{"district":"37081","name":"Guilford","country":"US"}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.district("37081") { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"district":"37081","name":"Guilford","country":"US","population":0,"water_area":0.25}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.district("37081") { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """
{"name":"Charlotte","country":"US","id":"city_123"}
""")
   val plain = ParseAPI("test") { transport = plainStub }.city("Charlotte")
   val stub0 = StubTransport(200, """
{"name":"Charlotte","country":"US","id":"city_123"}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.city("Charlotte") { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"name":"Charlotte","country":"US","id":"city_123","population":0,"area":0.25}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.city("Charlotte") { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """
{"postal":"28202","country":"US"}
""")
   val plain = ParseAPI("test") { transport = plainStub }.postal("28202")
   val stub0 = StubTransport(200, """
{"postal":"28202","country":"US"}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.postal("28202") { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"postal":"28202","country":"US","metros":[],"water_area":0.25,"tax_rate":0}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.postal("28202") { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }



  run {
   val plainStub = StubTransport(200, """
{"phone":"+14155552671","valid":true}
""")
   val plain = ParseAPI("test") { transport = plainStub }.phone("+14155552671")
   val stub0 = StubTransport(200, """
{"phone":"+14155552671","valid":true}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.phone("+14155552671") { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"phone":"+14155552671","valid":true,"state":"CA","timezone":"America/Los_Angeles"}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.phone("+14155552671") { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """
{"phone":"+14155552671","valid":true,"carrier":"Example"}
""")
   val plain = ParseAPI("test") { transport = plainStub }.carrier("+14155552671")
   val stub0 = StubTransport(200, """
{"phone":"+14155552671","valid":true,"carrier":"Example"}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.carrier("+14155552671") { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"phone":"+14155552671","valid":true,"carrier":"Example","city":"San Francisco","state":"CA"}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.carrier("+14155552671") { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """
{"phone":"+14155552671","valid":true,"live":true,"connected":false}
""")
   val plain = ParseAPI("test") { transport = plainStub }.hlr("+14155552671")
   val stub0 = StubTransport(200, """
{"phone":"+14155552671","valid":true,"live":true,"connected":false}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.hlr("+14155552671") { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"phone":"+14155552671","valid":true,"live":true,"connected":false,"roaming":false,"mcc":"310","mnc":"01"}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.hlr("+14155552671") { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }

  run {
   val plainStub = StubTransport(200, """
{"naics":"541511","name":"Programming","level":6,"parent":"54151","year":2022,"country":"US"}
""")
   val plain = ParseAPI("test") { transport = plainStub }.industry("541511")
   val stub0 = StubTransport(200, """
{"naics":"541511","name":"Programming","level":6,"parent":"54151","year":2022,"country":"US"}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.industry("541511") { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"naics":"541511","name":"Programming","level":6,"parent":"54151","year":2022,"country":"US","description":"Definition","children":[],"exclusions":[]}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.industry("541511") { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """
{"company":"01234567","valid":true,"name":"Example"}
""")
   val plain = ParseAPI("test") { transport = plainStub }.company("01234567")
   val stub0 = StubTransport(200, """
{"company":"01234567","valid":true,"name":"Example"}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.company("01234567") { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"company":"01234567","valid":true,"name":"Example","activity":"6201","gst":false,"vat":null}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.company("01234567") { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """
{"language":"en","name":"English","direction":"ltr"}
""")
   val plain = ParseAPI("test") { transport = plainStub }.language("en")
   val stub0 = StubTransport(200, """
{"language":"en","name":"English","direction":"ltr"}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.language("en") { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"language":"en","name":"English","direction":"ltr","iso3":"eng","countries":[]}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.language("en") { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """
{"name":"Andrea","valid":true,"first":"Andrea"}
""")
   val plain = ParseAPI("test") { transport = plainStub }.name("Andrea")
   val stub0 = StubTransport(200, """
{"name":"Andrea","valid":true,"first":"Andrea"}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.name("Andrea") { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"name":"Andrea","valid":true,"first":"Andrea","gender":null,"salutation":null}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.name("Andrea") { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """
{"timezone":"UTC","unix":0,"at":"1970-01-01T00:00:00+00:00","offset":"+00:00","dst":false}
""")
   val plain = ParseAPI("test") { transport = plainStub }.time("UTC")
   val stub0 = StubTransport(200, """
{"timezone":"UTC","unix":0,"at":"1970-01-01T00:00:00+00:00","offset":"+00:00","dst":false}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.time("UTC") { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"timezone":"UTC","unix":0,"at":"1970-01-01T00:00:00+00:00","offset":"+00:00","dst":false,"name":"UTC","offset_seconds":0,"offset_minutes":0,"next_dst":null}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.time("UTC") { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """
{"date":"1970-01-01","valid":true,"unix":0}
""")
   val plain = ParseAPI("test") { transport = plainStub }.date("1970-01-01")
   val stub0 = StubTransport(200, """
{"date":"1970-01-01","valid":true,"unix":0}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.date("1970-01-01") { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"date":"1970-01-01","valid":true,"unix":0,"year":1970,"weekday":4,"leap":false}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.date("1970-01-01") { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """
{"emoji":"😀","name":"grinning face","shortcodes":[]}
""")
   val plain = ParseAPI("test") { transport = plainStub }.emoji("😀")
   val stub0 = StubTransport(200, """
{"emoji":"😀","name":"grinning face","shortcodes":[]}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.emoji("😀") { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"emoji":"😀","name":"grinning face","shortcodes":[],"hex":"1F600","skins":[]}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.emoji("😀") { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """
{"latitude":0,"longitude":0,"timezone":"Etc/GMT"}
""")
   val plain = ParseAPI("test") { transport = plainStub }.point(0.0,0.0)
   val stub0 = StubTransport(200, """
{"latitude":0,"longitude":0,"timezone":"Etc/GMT"}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.point(0.0,0.0) { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"latitude":0,"longitude":0,"timezone":"Etc/GMT","elevation":0.25,"city":{"name":"Place","id":"city_123","type":"city","distance":0.5}}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.point(0.0,0.0) { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """
{"latitude":0,"longitude":0,"current":{"temperature":0,"observed_at":"2026-09-08T12:00:00Z"}}
""")
   val plain = ParseAPI("test") { transport = plainStub }.weather(0.0,0.0)
   val stub0 = StubTransport(200, """
{"latitude":0,"longitude":0,"current":{"temperature":0,"observed_at":"2026-09-08T12:00:00Z"}}
""")
   val rich0 = ParseAPI("test") { transport=stub0 }.weather(0.0,0.0) { deep=true }
   rich0
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """
{"latitude":0,"longitude":0,"current":{"temperature":0,"observed_at":"2026-09-08T12:00:00Z","pressure":1012.5,"wind_gust":0},"forecast":[]}
""")
   val rich1 = ParseAPI("test") { transport=stub1 }.weather(0.0,0.0) { deep=true }
   rich1
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
 }
 @Test fun childOperationsForwardDepthAndKeepPerRecordProfiles() = runBlocking {
  val rows = StubTransport(200, """
{"state":"NC","country":"US","districts":[{"district":"37081","name":"Guilford","population":0}]}
""")
  val state = ParseAPI("test") { transport=rows }.stateDistricts("NC") { country="US"; deep=true }
  assertEquals(0L,state.districts.single()?.population)
  assertEquals("https://api.parseapi.com/state/NC/districts?country=US&deep=true",rows.requests.single().url)
  val cityBody = """
{"name":"Place","country":"US","id":"city_123","distance":0,"distance_mi":0,"population":0}
"""
  val id = StubTransport(200,cityBody)
  assertEquals(0L,ParseAPI("test") { transport=id }.cityId("city_123") { deep=true }?.population)
  assertEquals("https://api.parseapi.com/city/id/city_123?deep=true",id.requests.single().url)
  val nearest = StubTransport(200,cityBody)
  assertEquals(0.0,ParseAPI("test") { transport=nearest }.cityNearest(0.0,0.0) { deep=true }.distance)
  assertEquals("https://api.parseapi.com/city?lat=0&lon=0&deep=true",nearest.requests.single().url)
  val cities = StubTransport(200,"""{"q":"place","cities":[$cityBody]}""")
  assertEquals(0L,ParseAPI("test") { transport=cities }.citySearch("place") { deep=true }.cities.single()?.population)
  assertEquals("https://api.parseapi.com/city?q=place&deep=true",cities.requests.single().url)
  val nearbyCity = StubTransport(200,"""{"city":"Place","country":"US","radius":10,"unit":"km","nearby":[$cityBody]}""")
  ParseAPI("test") { transport=nearbyCity }.cityNearby("Place") { deep=true }
  assertEquals("https://api.parseapi.com/city/Place/nearby?deep=true",nearbyCity.requests.single().url)
  val nearby = StubTransport(200,"""
{"postal":"28202","country":"US","radius":10,"unit":"km","nearby":[],"metros":[]}
""")
  assertEquals(0,ParseAPI("test") { transport=nearby }.postalNearby("28202") { deep=true }?.metros?.size)
  assertEquals("https://api.parseapi.com/postal/28202/nearby?deep=true",nearby.requests.single().url)
  val distance = StubTransport(200,"""
{"country":"US","from":{"postal":"28202","metros":[]},"to":{"postal":"10001","metros":null},"distance":0,"distance_mi":0}
""")
  val endpoints = ParseAPI("test") { transport=distance }.postalDistance("28202","10001") { deep=true }
  assertEquals(0,endpoints.from?.metros?.size)
  assertNull(endpoints.to?.metros)
  assertEquals("https://api.parseapi.com/postal/28202/distance/10001?deep=true",distance.requests.single().url)
  val naics = StubTransport(200,"""
{"q":"software","country":"US","year":2022,"results":[{"naics":"541511","name":"Programming","level":6,"children":[]}]}
""")
  assertEquals(0,ParseAPI("test") { transport=naics }.industrySearch("software") { deep=true }.results.single()?.children?.size)
  assertEquals("https://api.parseapi.com/industry?q=software&deep=true",naics.requests.single().url)
  val emoji = StubTransport(200,"""
{"q":"smile","emojis":[{"emoji":"😀","name":"grinning face","hex":"1F600"}]}
""")
  assertEquals("1F600",ParseAPI("test") { transport=emoji }.emojiSearch("smile") { deep=true }.emojis.single()?.hex)
  assertEquals("https://api.parseapi.com/emoji?q=smile&deep=true",emoji.requests.single().url)
 }

}
