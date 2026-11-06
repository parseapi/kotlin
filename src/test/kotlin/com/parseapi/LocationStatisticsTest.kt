package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class LocationStatisticsTest {
 private fun wire(core: String, detail: String): String = core.dropLast(1) + detail.trim().removePrefix("{").removeSuffix("}").let { if (it.isBlank()) "" else ",$it" } + "}"
 @Test fun preservesUnknownZeroAndOpenReasons() = runBlocking {
  for (deep in listOf("{}", """
{"population":null,"population_period":null,"property_tax":null}
""")) {
  val place = ParseAPI("test") { transport = StubTransport(200, wire("""{"postal":"12345","country":"US"}""", deep)) }.postal("12345")
   assertNull(place?.propertyTax)
   assertNull(place?.populationPeriod)
   assertNull(place?.population)
  }
  val deep = """{"population":0,"population_period":"2020-2024","property_tax":{"annual_median":0,"currency":"USD","period":"2020-2024"}}"""
  val postalDeep = """{"population":0,"currency":"USD","property_tax":{"annual_median":0}}"""
  val place = ParseAPI("test") { transport = StubTransport(200, wire("""{"postal":"12345","country":"US"}""", postalDeep)) }.postal("12345")
  assertEquals(0L, place?.population)
  assertNull(place?.populationPeriod)
  assertEquals(0.0, place?.propertyTax?.annualMedian)
  assertEquals("USD", place?.currency)
  val district = ParseAPI("test") { transport = StubTransport(200, wire("""{"district":"37081","name":"Guilford","country":"US"}""", deep)) }.district("37081")
  assertEquals("2020-2024", district?.propertyTax?.period)
  for (reason in listOf("", """, "reason":null""")) {
   val result = ParseAPI("test") { transport = StubTransport(200, """{"q":"a","addresses":[]$reason}""") }.addressSearch("a")
   assertNull(result.reason)
  }
  val result = ParseAPI("test") { transport = StubTransport(200, """
{"q":"a","addresses":[],"reason":"future_reason"}
""") }.addressSearch("a")
  assertEquals("future_reason", result.reason)
 }
}
