package com.parseapi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
class StackTest {
	@Test fun inventoryShapesAndEncoding() = runBlocking {
		val records = listOf(
			"""{"domain":"xn--bcher-kva.example","url":"https://xn--bcher-kva.example/","checked_at":null,"scope":"homepage","pages":0,"partial":null,"cms":null,"servers":null,"frameworks":null,"ecommerce":null,"analytics":null,"chat":null,"payments":null,"hosting":null,"future":true}""",
			"""{"domain":"xn--bcher-kva.example","url":"https://xn--bcher-kva.example/","checked_at":"2026-09-21T12:00:00Z","scope":"site","pages":2,"partial":false,"cms":[],"servers":[],"frameworks":[],"ecommerce":[],"analytics":[],"chat":[],"payments":[],"hosting":[],"future":true,"deep":{}}""",
			"""{"domain":"xn--bcher-kva.example","url":"https://xn--bcher-kva.example/","checked_at":"2026-09-21T12:00:00Z","scope":"site","pages":3,"partial":true,"cms":[{"technology":"wordpress","name":"WordPress","version":"6.8"},{"technology":"ghost","name":"Ghost","version":null}],"servers":[{"technology":"nginx","name":"nginx","version":"1.26.2"},{"technology":"apache","name":"Apache","version":null}],"frameworks":[{"technology":"nextjs","name":"Next.js","version":null,"future":true},{"technology":"react","name":"React","version":"19.1"}],"ecommerce":[{"technology":"woocommerce","name":"WooCommerce","version":null}],"analytics":[{"technology":"google-analytics","name":"Google Analytics","version":null}],"chat":[{"technology":"intercom","name":"Intercom","version":null}],"payments":[{"technology":"stripe","name":"Stripe","version":null}],"hosting":[{"technology":"vercel","name":"Vercel","version":null}],"future":true}""",
			"""{"domain":"xn--bcher-kva.example","url":"https://xn--bcher-kva.example/","checked_at":null,"scope":"future-scope","pages":0,"partial":null,"cms":null,"servers":null,"frameworks":null,"ecommerce":null,"analytics":null,"chat":null,"payments":null,"hosting":null,"future":true,"deep":{}}""",
			"""{"domain":"xn--bcher-kva.example","url":"https://xn--bcher-kva.example/","checked_at":"2026-09-21T12:00:00Z","scope":"homepage","pages":1,"partial":true,"cms":[],"servers":[],"frameworks":[{"technology":"nextjs","name":"Next.js","version":null,"future":true}],"ecommerce":[],"analytics":[],"chat":[],"payments":[],"hosting":[],"future":true,"deep":{}}"""
		)
		for ((i, body) in records.withIndex()) {
			val stub = StubTransport(200, body)
			val client = ParseAPI("test_key") { transport = stub; retries = 0; baseUrl = "https://api.parseapi.com" }
			val result = client.stack("bücher.example") { deep = true; pretty = true }
			assertEquals("https://api.parseapi.com/stack/b%C3%BCcher.example?deep=true&pretty=true", stub.requests[0].url)
			assertEquals("2.0.0", stub.requests[0].headers["Parse-Version"])
			if (i == 0 || i == 3) { assertNull(result.cms); assertNull(result.servers); assertEquals(0, result.pages); assertNull(result.partial) }
			else if (i != 2) { assertTrue(result.cms!!.isEmpty()); assertTrue(result.servers!!.isEmpty()) }
			for (group in listOf(result.ecommerce, result.analytics, result.chat, result.payments, result.hosting)) {
				if (i == 0 || i == 3) { assertNull(group) }
				else if (i == 2) { assertEquals(1, group!!.size); assertTrue(group.first().technology.isNotEmpty()) }
				else { assertTrue(group!!.isEmpty()) }
			}
			when (i) {
				0 -> { assertNull(result.frameworks); assertNull(result.checkedAt); assertNull(result.deep) }
				1 -> { assertEquals("site", result.scope); assertEquals(2, result.pages); assertEquals(false, result.partial); assertTrue(result.frameworks!!.isEmpty()); assertNotNull(result.deep) }
				2 -> {
					assertNotNull(result.checkedAt)
					assertEquals("site", result.scope); assertEquals(3, result.pages); assertEquals(true, result.partial)
					assertEquals(2, result.cms!!.size); assertEquals(2, result.servers!!.size); assertEquals(2, result.frameworks!!.size)
					assertEquals("wordpress", result.cms.first().technology); assertEquals("WordPress", result.cms.first().name); assertEquals("6.8", result.cms.first().version)
					assertEquals("ghost", result.cms.last().technology); assertNull(result.cms.last().version)
					assertEquals("1.26.2", result.servers.first().version); assertEquals("apache", result.servers.last().technology)
					assertNull(result.deep); assertNull(result.frameworks.first().version)
				}
				3 -> { assertEquals("future-scope", result.scope); assertNull(result.frameworks); assertNotNull(result.deep) }
				else -> {
					assertEquals("homepage", result.scope); assertEquals(1, result.pages); assertEquals(true, result.partial)
					assertNotNull(result.deep)
					assertEquals("nextjs", result.frameworks!!.first().technology)
					assertNull(result.frameworks.first().version)
				}
			}
			client.stack("example.com")
			assertEquals("https://api.parseapi.com/stack/example.com", stub.requests[1].url)
		}
	}
}

class StackDeadlineTest {
	@Test fun operationDefaultsAndExplicitSettings() = runBlocking {
		val body = """{"domain":"example.com","url":"https://example.com/","checked_at":null,"scope":"homepage","pages":0,"partial":null,"cms":null,"servers":null,"frameworks":null,"ecommerce":null,"analytics":null,"chat":null,"payments":null,"hosting":null,"deep":{},"available":false}"""
		for (configured in listOf(null, 10_000, 1_200, 45_000)) {
			val stub = StubTransport(200, body)
			val client = ParseAPI("test_key") {
				transport = stub
				if (configured != null) timeoutMs = configured
			}
			val result = client.stack("example.com")
			assertNull(result.frameworks)
			assertNotNull(result.deep)
			client.domain("example.com")
			client.stack("example.com") { deep = true }
			assertEquals(listOf(configured ?: 35_000, configured ?: 10_000, configured ?: 35_000), stub.requests.map { it.timeoutMs })
		}
	}
}
