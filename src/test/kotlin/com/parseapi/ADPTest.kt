package com.parseapi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking

class ADPTest {
 @Test fun modelsDecodeTriadThroughTheirOperations() = runBlocking {
  run {
   val plainStub = StubTransport(200, """{"country":"US","name":"United States","continent":"NA"}""")
   val plain = ParseAPI("test") { transport = plainStub }.country("US")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"country":"US","name":"United States","continent":"NA","deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.country("US") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"country":"US","name":"United States","continent":"NA","deep":{"iso3":"USA","population":0,"tax_rate":0,"plugs":[]}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.country("US") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"state":"NC","name":"North Carolina","country":"US"}""")
   val plain = ParseAPI("test") { transport = plainStub }.state("NC")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"state":"NC","name":"North Carolina","country":"US","deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.state("NC") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"state":"NC","name":"North Carolina","country":"US","deep":{"population":0,"area":0.25,"tax_rate":0}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.state("NC") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"district":"37081","name":"Guilford","country":"US"}""")
   val plain = ParseAPI("test") { transport = plainStub }.district("37081")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"district":"37081","name":"Guilford","country":"US","deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.district("37081") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"district":"37081","name":"Guilford","country":"US","deep":{"population":0,"water_area":0.25}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.district("37081") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"name":"Charlotte","country":"US","id":"city_123"}""")
   val plain = ParseAPI("test") { transport = plainStub }.city("Charlotte")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"name":"Charlotte","country":"US","id":"city_123","deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.city("Charlotte") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"name":"Charlotte","country":"US","id":"city_123","deep":{"population":0,"area":0.25}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.city("Charlotte") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"postal":"28202","country":"US"}""")
   val plain = ParseAPI("test") { transport = plainStub }.postal("28202")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"postal":"28202","country":"US","deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.postal("28202") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"postal":"28202","country":"US","deep":{"metros":[],"water_area":0.25,"tax_rate":0}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.postal("28202") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"iban":"DE89370400440532013000","valid":true}""")
   val plain = ParseAPI("test") { transport = plainStub }.iban("DE89370400440532013000")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"iban":"DE89370400440532013000","valid":true,"deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.iban("DE89370400440532013000") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"iban":"DE89370400440532013000","valid":true,"deep":{"checksum":"89","branch":null,"account":"0532013000"}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.iban("DE89370400440532013000") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"npi":"1881018208","valid":true,"excluded":true,"credential":"MD","state_name":"Minnesota"}""")
   val plain = ParseAPI("test") { transport = plainStub }.npi("1881018208")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"npi":"1881018208","valid":true,"excluded":true,"credential":"MD","state_name":"Minnesota","deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.npi("1881018208") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"npi":"1881018208","valid":true,"excluded":true,"credential":"MD","state_name":"Minnesota","deep":{"deactivated_at":"2026-09-01","enrollments":[]}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.npi("1881018208") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"vin":"1HGCM82633A004352","valid":true,"make":"Honda"}""")
   val plain = ParseAPI("test") { transport = plainStub }.vin("1HGCM82633A004352")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"vin":"1HGCM82633A004352","valid":true,"make":"Honda","deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.vin("1HGCM82633A004352") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"vin":"1HGCM82633A004352","valid":true,"make":"Honda","deep":{"horsepower":240.5,"recalls":[]}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.vin("1HGCM82633A004352") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"phone":"+14155552671","valid":true}""")
   val plain = ParseAPI("test") { transport = plainStub }.phone("+14155552671")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"phone":"+14155552671","valid":true,"deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.phone("+14155552671") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"phone":"+14155552671","valid":true,"deep":{"state":"CA","timezone":"America/Los_Angeles"}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.phone("+14155552671") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"phone":"+14155552671","valid":true,"carrier":"Example"}""")
   val plain = ParseAPI("test") { transport = plainStub }.carrier("+14155552671")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"phone":"+14155552671","valid":true,"carrier":"Example","deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.carrier("+14155552671") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"phone":"+14155552671","valid":true,"carrier":"Example","deep":{"city":"San Francisco","state":"CA"}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.carrier("+14155552671") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"phone":"+14155552671","valid":true,"live":true,"connected":false}""")
   val plain = ParseAPI("test") { transport = plainStub }.hlr("+14155552671")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"phone":"+14155552671","valid":true,"live":true,"connected":false,"deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.hlr("+14155552671") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"phone":"+14155552671","valid":true,"live":true,"connected":false,"deep":{"roaming":false,"mcc":"310","mnc":"01"}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.hlr("+14155552671") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"hts":"8471.30.01.00","description":"Portable computers","revision":"2026"}""")
   val plain = ParseAPI("test") { transport = plainStub }.tariff("8471.30.01.00")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"hts":"8471.30.01.00","description":"Portable computers","revision":"2026","deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.tariff("8471.30.01.00") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"hts":"8471.30.01.00","description":"Portable computers","revision":"2026","deep":{"units":[],"special":"Free","origin":null,"effective_rate":null,"measures":null}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.tariff("8471.30.01.00") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"naics":"541511","name":"Programming","level":6,"parent":"54151","year":2022,"country":"US"}""")
   val plain = ParseAPI("test") { transport = plainStub }.naics("541511")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"naics":"541511","name":"Programming","level":6,"parent":"54151","year":2022,"country":"US","deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.naics("541511") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"naics":"541511","name":"Programming","level":6,"parent":"54151","year":2022,"country":"US","deep":{"description":"Definition","children":[],"exclusions":[]}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.naics("541511") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"company":"01234567","valid":true,"name":"Example"}""")
   val plain = ParseAPI("test") { transport = plainStub }.company("01234567")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"company":"01234567","valid":true,"name":"Example","deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.company("01234567") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"company":"01234567","valid":true,"name":"Example","deep":{"activity":"6201","gst":false,"vat":null}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.company("01234567") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"currency":"USD","name":"US Dollar"}""")
   val plain = ParseAPI("test") { transport = plainStub }.currency("USD")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"currency":"USD","name":"US Dollar","deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.currency("USD") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"currency":"USD","name":"US Dollar","deep":{"numeric":840,"countries":[]}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.currency("USD") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"language":"en","name":"English","direction":"ltr"}""")
   val plain = ParseAPI("test") { transport = plainStub }.language("en")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"language":"en","name":"English","direction":"ltr","deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.language("en") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"language":"en","name":"English","direction":"ltr","deep":{"iso3":"eng","countries":[]}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.language("en") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"name":"Andrea","valid":true,"first":"Andrea"}""")
   val plain = ParseAPI("test") { transport = plainStub }.name("Andrea")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"name":"Andrea","valid":true,"first":"Andrea","deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.name("Andrea") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"name":"Andrea","valid":true,"first":"Andrea","deep":{"known":false,"gender":null,"countries":[]}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.name("Andrea") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"timezone":"UTC","unix":0,"at":"1970-01-01T00:00:00+00:00","offset":"+00:00","dst":false}""")
   val plain = ParseAPI("test") { transport = plainStub }.time("UTC")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"timezone":"UTC","unix":0,"at":"1970-01-01T00:00:00+00:00","offset":"+00:00","dst":false,"deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.time("UTC") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"timezone":"UTC","unix":0,"at":"1970-01-01T00:00:00+00:00","offset":"+00:00","dst":false,"deep":{"name":"UTC","offset_seconds":0,"offset_minutes":0,"next_dst":null}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.time("UTC") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"date":"1970-01-01","valid":true,"unix":0}""")
   val plain = ParseAPI("test") { transport = plainStub }.date("1970-01-01")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"date":"1970-01-01","valid":true,"unix":0,"deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.date("1970-01-01") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"date":"1970-01-01","valid":true,"unix":0,"deep":{"year":1970,"weekday":4,"leap":false}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.date("1970-01-01") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"emoji":"😀","name":"grinning face","shortcodes":[]}""")
   val plain = ParseAPI("test") { transport = plainStub }.emoji("😀")
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"emoji":"😀","name":"grinning face","shortcodes":[],"deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.emoji("😀") { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"emoji":"😀","name":"grinning face","shortcodes":[],"deep":{"hex":"1F600","skins":[]}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.emoji("😀") { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"latitude":0,"longitude":0,"timezone":"Etc/GMT"}""")
   val plain = ParseAPI("test") { transport = plainStub }.point(0.0,0.0)
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"latitude":0,"longitude":0,"timezone":"Etc/GMT","deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.point(0.0,0.0) { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"latitude":0,"longitude":0,"timezone":"Etc/GMT","deep":{"elevation":0.25,"city":{"name":"Place","id":"city_123","type":"city","distance":0.5}}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.point(0.0,0.0) { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
  run {
   val plainStub = StubTransport(200, """{"latitude":0,"longitude":0,"current":{"temperature":0,"observed_at":"2026-09-08T12:00:00Z"}}""")
   val plain = ParseAPI("test") { transport = plainStub }.weather(0.0,0.0)
   assertNull(plain.deep)
   val stub0 = StubTransport(200, """{"latitude":0,"longitude":0,"current":{"temperature":0,"observed_at":"2026-09-08T12:00:00Z"},"deep":{}}""")
   val rich0 = ParseAPI("test") { transport=stub0 }.weather(0.0,0.0) { deep=true }
   assertNotNull(rich0.deep)
   assertEquals(1,stub0.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub0.requests.single().url)
   val stub1 = StubTransport(200, """{"latitude":0,"longitude":0,"current":{"temperature":0,"observed_at":"2026-09-08T12:00:00Z"},"deep":{"current":{"pressure":1012.5,"wind_gust":0},"forecast":[]}}""")
   val rich1 = ParseAPI("test") { transport=stub1 }.weather(0.0,0.0) { deep=true }
   assertNotNull(rich1.deep)
   assertEquals(1,stub1.requests.size)
   assertEquals(plainStub.requests.single().url+(if (plainStub.requests.single().url.contains("?")) "&" else "?")+"deep=true",stub1.requests.single().url)
  }
 }
 @Test fun childOperationsForwardDepthAndKeepPerRecordProfiles() = runBlocking {
  val rows = StubTransport(200, """{"state":"NC","country":"US","districts":[{"district":"37081","name":"Guilford","deep":{"population":0}}]}""")
  val state = ParseAPI("test") { transport=rows }.stateDistricts("NC") { country="US"; deep=true }
  assertEquals(0L,state.districts.single().deep?.population)
  assertEquals("https://api.parseapi.com/state/NC/districts?country=US&deep=true",rows.requests.single().url)
  val cityBody = """{"name":"Place","country":"US","id":"city_123","distance":0,"distance_mi":0,"deep":{"population":0}}"""
  val id = StubTransport(200,cityBody)
  assertEquals(0L,ParseAPI("test") { transport=id }.cityId("city_123") { deep=true }.deep?.population)
  assertEquals("https://api.parseapi.com/city/id/city_123?deep=true",id.requests.single().url)
  val nearest = StubTransport(200,cityBody)
  assertEquals(0.0,ParseAPI("test") { transport=nearest }.cityNearest(0.0,0.0) { deep=true }.distance)
  assertEquals("https://api.parseapi.com/city?lat=0&lon=0&deep=true",nearest.requests.single().url)
  val cities = StubTransport(200,"""{"q":"place","cities":[$cityBody]}""")
  assertEquals(0L,ParseAPI("test") { transport=cities }.citySearch("place") { deep=true }.cities.single().deep?.population)
  assertEquals("https://api.parseapi.com/city?q=place&deep=true",cities.requests.single().url)
  val nearbyCity = StubTransport(200,"""{"city":"Place","country":"US","radius":10,"unit":"km","nearby":[$cityBody]}""")
  ParseAPI("test") { transport=nearbyCity }.cityNearby("Place") { deep=true }
  assertEquals("https://api.parseapi.com/city/Place/nearby?deep=true",nearbyCity.requests.single().url)
  val nearby = StubTransport(200,"""{"postal":"28202","country":"US","radius":10,"unit":"km","nearby":[],"deep":{"metros":[]}}""")
  assertEquals(0,ParseAPI("test") { transport=nearby }.postalNearby("28202") { deep=true }.deep?.metros?.size)
  assertEquals("https://api.parseapi.com/postal/28202/nearby?deep=true",nearby.requests.single().url)
  val distance = StubTransport(200,"""{"country":"US","from":{"postal":"28202","deep":{"metros":[]}},"to":{"postal":"10001","deep":{"metros":null}},"distance":0,"distance_mi":0}""")
  val endpoints = ParseAPI("test") { transport=distance }.postalDistance("28202","10001") { deep=true }
  assertEquals(0,endpoints.from.deep?.metros?.size)
  assertNull(endpoints.to.deep?.metros)
  assertEquals("https://api.parseapi.com/postal/28202/distance/10001?deep=true",distance.requests.single().url)
  val naics = StubTransport(200,"""{"q":"software","country":"US","year":2022,"results":[{"naics":"541511","name":"Programming","level":6,"deep":{"children":[]}}]}""")
  assertEquals(0,ParseAPI("test") { transport=naics }.naicsSearch("software") { deep=true }.results.single().deep?.children?.size)
  assertEquals("https://api.parseapi.com/naics?q=software&deep=true",naics.requests.single().url)
  val emoji = StubTransport(200,"""{"q":"smile","emojis":[{"emoji":"😀","name":"grinning face","deep":{"hex":"1F600"}}]}""")
  assertEquals("1F600",ParseAPI("test") { transport=emoji }.emojiSearch("smile") { deep=true }.emojis.single().deep?.hex)
  assertEquals("https://api.parseapi.com/emoji?q=smile&deep=true",emoji.requests.single().url)
 }

}
