package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class TaxTest {
 @Test fun deepTaxReferencesPreserveDecimalsAndZero() = runBlocking {
  val country = ParseAPI("test") { transport = StubTransport(200, """{"country":"DE","name":"Germany","continent":"EU","deep":{"tax":"VAT","tax_rate":19,"tax_id_format":"DE999999999","tax_id_regex":"^DE[0-9]{9}$"}}""") }.country("DE") { deep = true }
  assertEquals("VAT", country.deep?.tax)
  assertEquals(19.0, country.deep?.taxRate)
  assertEquals("DE999999999", country.deep?.taxIdFormat)
  assertEquals("^DE[0-9]{9}$", country.deep?.taxIdRegex)
  val postal = ParseAPI("test") { transport = StubTransport(200, """{"postal":"12345","country":"US","deep":{"tax":"Sales tax","tax_rate":7.9,"tax_rate_state":5,"tax_rate_county":0,"tax_rate_city":null,"tax_rate_special":2.9}}""") }.postal("12345") { deep = true }
  assertEquals(7.9, postal.deep?.taxRate)
  assertEquals(5.0, postal.deep?.taxRateState)
  assertEquals(0.0, postal.deep?.taxRateCounty)
  assertNull(postal.deep?.taxRateCity)
  assertEquals(2.9, postal.deep?.taxRateSpecial)
 }
 @Test fun absentLockedAndNullTaxRemainUnknown() = runBlocking {
  for (field in listOf("", """, "deep":{}""", """, "deep":{"tax":null,"tax_rate":null}""")) {
   val postal = ParseAPI("test") { transport = StubTransport(200, """{"postal":"12345","country":"US"$field}""") }.postal("12345")
   assertNull(postal.deep?.tax)
   assertNull(postal.deep?.taxRate)
   assertNull(postal.deep?.taxRateSpecial)
   val country = ParseAPI("test") { transport = StubTransport(200, """{"country":"DE","name":"Germany","continent":"EU"$field}""") }.country("DE")
   assertNull(country.deep?.tax)
   assertNull(country.deep?.taxRate)
   assertNull(country.deep?.taxIdFormat)
   assertNull(country.deep?.taxIdRegex)
  }
 }
}
