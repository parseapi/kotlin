package com.parseapi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class ProviderDeepTest {
 @Test fun publishedDetails() = runBlocking {
  val stub = StubTransport(200, """{"npi":"1881018208","valid":true,"sources":{"nppes":{"edition":"example","published_at":null}},"deep":{"updated_at":"2026-09-18","taxonomies":[{"taxonomy":"207Q00000X","primary":true,"license":"000123"}]}}""")
  val value = ParseAPI("test") { transport = stub }.provider("1881018208") { deep = true }
  assertEquals("000123", value.deep?.taxonomies?.first()?.license)
  assertEquals("2026-09-18", value.deep?.updatedAt)
  assertNull(value.sources?.nppes?.publishedAt)
 }
}
