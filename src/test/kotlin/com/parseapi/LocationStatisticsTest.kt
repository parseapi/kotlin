package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class LocationStatisticsTest {
 @Test fun preservesUnknownZeroAndOpenReasons() = runBlocking {
  for (deep in listOf("{}", """{"population":null,"population_period":null,"property_tax":null}""")) {
   val place = ParseAPI("test") { transport = StubTransport(200, """{"postal":"12345","country":"US","deep":$deep}""") }.postal("12345")
   assertNull(place.deep?.propertyTax)
   assertNull(place.deep?.populationPeriod)
   assertNull(place.deep?.population)
  }
  val deep = """{"population":0,"population_period":"2020-2024","property_tax":{"annual_median":0,"currency":"USD","period":"2020-2024"}}"""
  val place = ParseAPI("test") { transport = StubTransport(200, """{"postal":"12345","country":"US","deep":$deep}""") }.postal("12345") { country = "US"; this.deep = true }
  assertEquals(0L, place.deep?.population)
  assertEquals("2020-2024", place.deep?.populationPeriod)
  assertEquals(0.0, place.deep?.propertyTax?.annualMedian)
  assertEquals("USD", place.deep?.propertyTax?.currency)
  assertEquals("2020-2024", place.deep?.propertyTax?.period)
  val district = ParseAPI("test") { transport = StubTransport(200, """{"district":"37081","name":"Guilford","country":"US","deep":$deep}""") }.district("37081")
  assertEquals("2020-2024", district.deep?.propertyTax?.period)
  for (reason in listOf("", """, "reason":null""")) {
   val result = ParseAPI("test") { transport = StubTransport(200, """{"q":"a","addresses":[]$reason}""") }.addressSearch("a")
   assertNull(result.reason)
  }
  val result = ParseAPI("test") { transport = StubTransport(200, """{"q":"a","addresses":[],"reason":"future_reason"}""") }.addressSearch("a")
  assertEquals("future_reason", result.reason)
 }
}
