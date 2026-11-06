package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class TaxTest {
 @Test fun deepTaxReferencesPreserveDecimalsAndZero() = runBlocking {
  val country = ParseAPI("test") { transport = StubTransport(200, """
{"country":"DE","name":"Germany","continent":"EU","tax":{"name":"VAT","rate":19}}
""") }.country("DE") { deep = true }
  assertEquals("VAT", country?.tax?.name)
  assertEquals(19.0, country?.tax?.rate)
  val postal = ParseAPI("test") { transport = StubTransport(200, """
{"postal":"12345","country":"US","tax":{"name":"Sales tax","rate":7.9,"rate_state":5,"rate_county":0,"rate_city":null,"rate_special":2.9}}
""") }.postal("12345") { deep = true }
  assertEquals(7.9, postal?.tax?.rate)
  assertEquals(5.0, postal?.tax?.rateState)
  assertEquals(0.0, postal?.tax?.rateCounty)
  assertNull(postal?.tax?.rateCity)
  assertEquals(2.9, postal?.tax?.rateSpecial)
 }
 @Test fun absentLockedAndNullTaxRemainUnknown() = runBlocking {
  for (field in listOf("", """, "deep":{}""", """, "deep":{"tax":null,"tax_rate":null}""")) {
   val postal = ParseAPI("test") { transport = StubTransport(200, """{"postal":"12345","country":"US"$field}""") }.postal("12345")
   assertNull(postal?.tax)
   assertNull(postal?.tax?.rate)
   assertNull(postal?.tax?.rateSpecial)
   val country = ParseAPI("test") { transport = StubTransport(200, """{"country":"DE","name":"Germany","continent":"EU"$field}""") }.country("DE")
   assertNull(country?.tax?.name)
   assertNull(country?.tax?.rate)
  }
 }
}
