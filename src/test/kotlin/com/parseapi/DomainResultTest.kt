package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlin.test.*

class DomainResultTest {
	private fun client(stub: StubTransport) = ParseAPI("test_key") { transport = stub; retries = 0 }

	@Test fun nullableAvailabilityAndPartialDetailUseOneLookup() = runBlocking {
		val cases = listOf(
			"""{"domain":"x.example","available":null,"deep":{}}""" to null,
			"""{"domain":"x.example"}""" to null,
			"""{"domain":"x.example","available":true,"deep":{"registration":{"registered":false}}}""" to true,
			"""{"domain":"x.example","available":false,"deep":{"registration":{"registered":true,"created":null,"status":[],"dnssec":false}}}""" to false,
			// Tolerant decoding; unavailable current responses normally use deep:{}.
			"""{"domain":"x.example","available":false,"deep":{"registration":null}}""" to false,
			"""{"domain":"x.example","available":false,"deep":{"registration":{"registered":true,"status":null,"dnssec":null}}}""" to false,
			"""{"domain":"x.example","available":false,"deep":{"registration":{}}}""" to false,
			"""{"domain":"x.example","available":false,"deep":{"registration":{"registered":null}}}""" to false,
		)
		for ((index, item) in cases.withIndex()) {
			val stub = StubTransport(200, item.first)
			val parse = client(stub)
			val lookup: suspend (String, DomainOptions.() -> Unit) -> DomainResult = parse::domainResult
			val result = lookup("bücher.example") { deep = true }
			val available: Boolean? = result.available
			assertEquals(item.second, available)
			assertEquals("x.example", result.domain)
			assertEquals(1, stub.requests.size)
			assertEquals("https://api.parseapi.com/domain/b%C3%BCcher.example?deep=true", stub.requests.single().url)
			assertEquals("2.0.0", stub.requests.single().headers["Parse-Version"])
			assertEquals("test_key", stub.requests.single().headers["X-API-Key"])
			when (index) {
				0 -> { assertNotNull(result.deep); assertNull(result.deep.registration) }
				1 -> assertNull(result.deep)
				2 -> { assertEquals(false, result.deep?.registration?.registered); assertNull(result.deep?.registration?.dnssec); assertNull(result.deep?.registration?.status) }
				3 -> { assertEquals(true, result.deep?.registration?.registered); assertEquals(false, result.deep?.registration?.dnssec); assertEquals(emptyList(), result.deep?.registration?.status) }
				4 -> { assertNotNull(result.deep); assertNull(result.deep.registration) }
				5 -> { assertEquals(true, result.deep?.registration?.registered); assertNull(result.deep?.registration?.dnssec); assertNull(result.deep?.registration?.status) }
				6, 7 -> { assertNotNull(result.deep?.registration); assertNull(result.deep?.registration?.registered) }
			}
		}
	}

	@Test fun existingDomainCallablePreservesUnknownAndKnownAvailability() = runBlocking {
        for ((raw, expected) in listOf("true" to true, "false" to false, "null" to null)) {
            for (signing in listOf("", ",\"dnssec\":null", ",\"dnssec\":false", ",\"dnssec\":true")) {
                val detail = when (raw) { "null" -> "{}"; "true" -> """{"registration":{"registered":false}}"""; else -> """{"registration":{"registered":true$signing}}""" }
                val stub = StubTransport(200, """{"domain":"x.example","available":$raw,"deep":$detail}""")
                val parse = client(stub)
                val lookup: suspend (String, DomainOptions.() -> Unit) -> Domain = parse::domain
                val result = lookup("x.example") { deep = true }
                val available: Boolean? = result.available
                assertEquals(expected, available)
                val expectedSigning = when { raw != "false" -> null; signing.contains("false") -> false; signing.contains("true") -> true; else -> null }
                assertEquals(expectedSigning, result.deep?.registration?.dnssec)
                assertEquals(1, stub.requests.size)
            }
        }
        val stub = StubTransport(200, """{"domain":"x.example","available":"not-a-boolean"}""")
        assertFailsWith<SerializationException> { client(stub).domain("x.example") }
        assertEquals(1, stub.requests.size, "A decode failure does not repeat the lookup")
    }

	@Test fun noDeepOverloadsAndExplicitFalseRemainAvailable() = runBlocking {
		val stub = StubTransport(200, """{"domain":"x.example","available":false}""")
		val parse = client(stub)
		val old: suspend (String) -> Domain = parse::domain
		val lookup: suspend (String) -> DomainResult = parse::domainResult
		val result = lookup("x.example")
		assertEquals(false, result.available); assertNull(result.deep)
		assertEquals(false, old("x.example").available)
		assertEquals(2, stub.requests.size)
		assertTrue(stub.requests.all { it.url == "https://api.parseapi.com/domain/x.example" })
	}
}
