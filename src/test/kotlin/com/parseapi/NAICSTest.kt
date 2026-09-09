package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NAICSTest {
 @Test fun hierarchyAndSearch() = runBlocking {
  val stub = StubTransport(mutableListOf(
   ParseAPIResponse(200, """{"naics":"31-33","name":"Manufacturing","description":null,"level":2,"parent":null,"parent_name":null,"children":[{"naics":"311","name":"Food Manufacturing"}],"year":2022,"country":"US","future":true}""", emptyMap()),
   ParseAPIResponse(200, """{"q":"coffee & tea","year":2022,"country":"US","results":[]}""", emptyMap())
  ))
  val c = ParseAPI("test_key") { transport = stub; retries = 0 }
  val industry = c.naics("31-33")
  assertEquals("31-33", industry.naics)
  assertNull(industry.description)
  assertNull(industry.parent)
  assertEquals("311", industry.children[0].naics)
  val search = c.naicsSearch("coffee & tea") { limit = 5 }
  assertEquals(2022, search.year)
  assertTrue(search.results.isEmpty())
  assertEquals("https://api.parseapi.com/naics/31-33", stub.requests[0].url)
  assertEquals("https://api.parseapi.com/naics?q=coffee%20%26%20tea&limit=5", stub.requests[1].url)
 }
}
