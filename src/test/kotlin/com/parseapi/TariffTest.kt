package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlin.test.*

class TariffTest {
	@Test fun searchPreservesParentContextAndOlderResponses() = runBlocking {
		for (suffix in listOf("", """, "lineage":null""")) {
			val body = """{"q":"horses","revision":"fixture","lines":[{"hts":"0101.29.00.90","description":"Other","general":null""" + suffix + "}]}"
			val stub = StubTransport(200, body)
			val result = ParseAPI("test") { transport = stub }.tariffSearch("horses")
			assertNull(result.lines[0].lineage)
		}
		val stub = StubTransport(200, """{"q":"horses & ponies","revision":"fixture","lines":[{"hts":"0101.29.00.90","description":"Other","general":null,"lineage":["Live horses","Other horses"],"future":true},{"hts":"0101","description":"Live horses","general":null,"lineage":[]}]}""")
		val result = ParseAPI("test") { transport = stub }.tariffSearch("horses & ponies")
		assertEquals(listOf("Live horses", "Other horses"), result.lines[0].lineage)
		assertEquals(emptyList(), result.lines[1].lineage)
		assertEquals("https://api.parseapi.com/tariff?q=horses%20%26%20ponies", stub.requests.single().url)
	}

	@Test fun editionDateRoundtrip() = runBlocking {
		val edition = "a".repeat(64)
		val stub = StubTransport(200, """{"hts":"0101","description":"Horses","lineage":[],"revision":"fixture","edition":"$edition","date":"2026-09-15","deep":{"effective_rate":null,"reason":"future_reason","measures":[]}}""")
		val result = ParseAPI("test") { transport = stub }.tariff("0101") { deep = true; origin = "CA"; this.edition = edition; date = "2026-09-15" }
		assertEquals(edition, result.edition)
		assertEquals("2026-09-15", result.date)
		assertEquals("future_reason", result.deep?.reason)
		assertNull(result.deep?.effectiveRate)
		assertEquals("https://api.parseapi.com/tariff/0101?origin=CA&edition=$edition&date=2026-09-15&deep=true", stub.requests.single().url)
		val searchStub = StubTransport(200, """{"q":"horses","revision":"fixture","edition":"$edition","date":"2026-09-15","lines":[]}""")
		val search = ParseAPI("test") { transport = searchStub }.tariffSearch("horses") { this.edition = edition; date = "2026-09-15" }
		assertEquals(edition, search.edition)
		assertEquals("2026-09-15", search.date)
		assertEquals("https://api.parseapi.com/tariff?q=horses&edition=$edition&date=2026-09-15", searchStub.requests.single().url)
		val oldStub = StubTransport(200, """{"hts":"0101","description":"Horses","revision":"old","deep":{}}""")
		val old = ParseAPI("test") { transport = oldStub }.tariff("0101")
		assertNull(old.edition); assertNull(old.date); assertNull(old.deep?.reason)
	}

	@Test fun ignoredSelectionIsRejected() = runBlocking {
		val lookup = ParseAPI("test") { transport = StubTransport(200, """{"hts":"0101","description":"Horses","revision":"old"}""") }
		val lookupError = assertFailsWith<ParseAPIException> { lookup.tariff("0101") { edition = "a".repeat(64) } }
		assertEquals("tariff_selection_mismatch", lookupError.code)
		assertEquals(0, lookupError.status)
		val search = ParseAPI("test") { transport = StubTransport(200, """{"q":"horses","revision":"old","lines":[]}""") }
		val searchError = assertFailsWith<ParseAPIException> { search.tariffSearch("horses") { date = "2026-09-15" } }
		assertEquals("tariff_selection_mismatch", searchError.code)
	}

	@Test fun dateSelectionRequiresExactEditionFingerprint() = runBlocking {
		for (returnedEdition in listOf("legacy", "", "A".repeat(64), "a".repeat(64) + "\n")) {
			val encodedEdition = returnedEdition.replace("\n", "\\n")
			val lookup = ParseAPI("test") { transport = StubTransport(200, """{"hts":"0101","description":"Horses","revision":"fixture","edition":"$encodedEdition","date":"2026-09-15"}""") }
			val lookupError = assertFailsWith<ParseAPIException> { lookup.tariff("0101") { date = "2026-09-15" } }
			assertEquals("tariff_selection_mismatch", lookupError.code)
			assertEquals(0, lookupError.status)
			val search = ParseAPI("test") { transport = StubTransport(200, """{"q":"horses","revision":"fixture","edition":"$encodedEdition","date":"2026-09-15","lines":[]}""") }
			val searchError = assertFailsWith<ParseAPIException> { search.tariffSearch("horses") { date = "2026-09-15" } }
			assertEquals("tariff_selection_mismatch", searchError.code)
			assertEquals(0, searchError.status)
		}
	}

	@Test fun explicitScopeAllowsConfirmedDateOrUndatedEdition() = runBlocking {
		val returnedEdition = "0123456789abcdef".repeat(4)
		for (returnedDate in listOf(null, "2026-09-15")) {
			val dateJSON = returnedDate?.let { "\"$it\"" } ?: "null"
			val requestedEdition = if (returnedDate == null) returnedEdition else null
			val lookup = ParseAPI("test") { transport = StubTransport(200, """{"hts":"0101","description":"Horses","revision":"fixture","edition":"$returnedEdition","date":$dateJSON,"deep":{}}""") }
			val result = lookup.tariff("0101") { deep = true; edition = requestedEdition; date = returnedDate }
			assertEquals(returnedEdition, result.edition)
			assertEquals(returnedDate, result.date)
			assertNull(result.deep?.reason)
			val search = ParseAPI("test") { transport = StubTransport(200, """{"q":"horses","revision":"fixture","edition":"$returnedEdition","date":$dateJSON,"lines":[]}""") }
			val results = search.tariffSearch("horses") { edition = requestedEdition; date = returnedDate }
			assertEquals(returnedEdition, results.edition)
			assertEquals(returnedDate, results.date)
		}
	}
}
