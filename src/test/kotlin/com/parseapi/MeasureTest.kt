package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class MeasureTest {
 private fun client(stub: StubTransport) = ParseAPI("test_key") { transport = stub; retries = 0 }

 @Test fun precisionAndEncoding() = runBlocking {
  val stub = StubTransport(200, """{"measure":"5 ft 11 in","valid":true,"type":"future-type","amount":"180.34000000000000000001","unit":"cm","reason":null,"choices":[],"future":null}""")
  val c = client(stub)
  val result = c.measure("5 ft 11 in") { to = "cm"; locale = "en-US"; system = "us" }
  assertEquals("180.34000000000000000001", result.amount)
  assertEquals("future-type", result.type)
  assertEquals("https://api.parseapi.com/measure/5%20ft%2011%20in?to=cm&locale=en-US&system=us", stub.requests[0].url)
  c.measure("1 kg/m^3") { to = "g/L" }
  assertEquals("https://api.parseapi.com/measure/1%20kg%2Fm%5E3?to=g%2FL", stub.requests[1].url)
 }

 @Test fun ambiguityAndDiscovery() = runBlocking {
  val stub = StubTransport(mutableListOf(
   ParseAPIResponse(200, """{"measure":"1 gallon","valid":false,"type":null,"amount":null,"unit":null,"reason":"ambiguous_unit","choices":[{"unit":"us_gal","name":"US liquid gallon"}]}""", emptyMap()),
   ParseAPIResponse(200, """{"units":[{"unit":"m","name":"metre","type":"length","aliases":["meter"],"future":true}]}""", emptyMap())
  ))
  val c = client(stub)
  val invalid = c.measure("1 gallon")
  assertFalse(invalid.valid)
  assertNull(invalid.amount)
  assertNull(invalid.type)
  assertEquals("ambiguous_unit", invalid.reason)
  assertEquals("us_gal", invalid.choices[0].unit)
  val units = c.measureUnits { query = "US gallon"; type = "volume"; unit = "L" }
  assertEquals(listOf("meter"), units.units[0].aliases)
  assertEquals("https://api.parseapi.com/measure/units?q=US%20gallon&type=volume&unit=L", stub.requests[1].url)
  c.measureUnits()
  assertEquals("https://api.parseapi.com/measure/units", stub.requests[2].url)
 }

 @Test fun badTargetUsesApiError() = runBlocking {
  val stub = StubTransport(400, """{"code":"bad_request","message":"Incompatible units","request_id":"req_measure"}""")
  val error = assertFailsWith<ParseAPIException> { client(stub).measure("1 m") { to = "kg" } }
  assertEquals("bad_request", error.code)
  assertEquals(1, stub.requests.size)
 }
}
