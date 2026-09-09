package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NAICSTest {
 @Test fun hierarchyAndSearch() = runBlocking {
  val stub = StubTransport(mutableListOf(
   ParseAPIResponse(200, """{"naics":"31-33","name":"Manufacturing","level":2,"parent":null,"parent_name":null,"year":2022,"country":"US","future":true,"deep":{"description":null,"children":[{"naics":"311","name":"Food Manufacturing"}]}}""", emptyMap()),
   ParseAPIResponse(200, """{"q":"coffee & tea","year":2022,"country":"US","results":[]}""", emptyMap())
  ))
  val c = ParseAPI("test_key") { transport = stub; retries = 0 }
  val industry = c.naics("31-33")
  assertEquals("31-33", industry.naics)
  assertNull(industry.deep?.description)
  assertNull(industry.parent)
  assertEquals("311", industry.deep!!.children!![0].naics)
  val search = c.naicsSearch("coffee & tea") { limit = 5 }
  assertEquals(2022, search.year)
  assertTrue(search.results.isEmpty())
  assertEquals("https://api.parseapi.com/naics/31-33", stub.requests[0].url)
  assertEquals("https://api.parseapi.com/naics?q=coffee%20%26%20tea&limit=5", stub.requests[1].url)
 }
}


class NAICSEvidenceTest {
 @Test fun exclusionsAndMatchRemainCompatible() = runBlocking {
  val stub = StubTransport(mutableListOf(ParseAPIResponse(200, """{"q":"sofware","year":2022,"country":"US","results":[{"naics":"541511","name":"Custom Computer Programming Services","level":6,"parent":"54151","parent_name":"Computer Systems Design and Related Services","deep":{"description":null,"children":[]}},{"naics":"541511","name":"Custom Computer Programming Services","level":6,"parent":"54151","parent_name":"Computer Systems Design and Related Services","match":null,"deep":{"description":null,"children":[],"exclusions":null}},{"naics":"541511","name":"Custom Computer Programming Services","level":6,"parent":"54151","parent_name":"Computer Systems Design and Related Services","match":{"field":"future-field","text":"Future matching evidence","corrections":[],"future":true},"deep":{"description":null,"children":[],"exclusions":[]}},{"naics":"541511","name":"Custom Computer Programming Services","level":6,"parent":"54151","parent_name":"Computer Systems Design and Related Services","match":{"field":"term","text":"Computer software programming services","corrections":[{"from":"sofware","to":"software"}]},"future":true,"deep":{"description":null,"children":[],"exclusions":[{"description":"Designing integrated computer systems","codes":[{"naics":"541512","name":"Computer Systems Design Services"}]},{"description":"Activities classified elsewhere","codes":[]}]}}]}""", emptyMap())))
  val client = ParseAPI("test_key") { transport = stub; retries = 0 }
  val search = client.naicsSearch("sofware")
  val results: List<NAICSSearchResult> = search.results
  assertNull(results[0].deep!!.exclusions)
  assertNull(results[0].match)
  assertNull(results[1].deep!!.exclusions)
  assertNull(results[1].match)
  assertTrue(results[2].deep!!.exclusions!!.isEmpty())
  assertEquals("future-field", results[2].match!!.field)
  assertTrue(results[2].match!!.corrections.isEmpty())
  assertEquals("541512", results[3].deep!!.exclusions!![0].codes[0].naics)
  assertEquals("Activities classified elsewhere", results[3].deep!!.exclusions!![1].description)
  assertTrue(results[3].deep!!.exclusions!![1].codes.isEmpty())
  assertEquals("Computer software programming services", results[3].match!!.text)
  assertEquals("sofware", results[3].match!!.corrections[0].from)
  assertEquals("software", results[3].match!!.corrections[0].to)
 }
}
