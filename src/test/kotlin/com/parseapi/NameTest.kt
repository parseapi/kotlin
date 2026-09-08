package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NameTest {
	@Test fun countryAndMembershipAreAdditive() = runBlocking {
		val stub = StubTransport(200, """{"name":"王","valid":true,"known":true,"countries":["CN","TW"],"gender":null,"future":true}""")
		val client = ParseAPI("test_key") { transport = stub; retries = 0 }
		val oldCall: suspend (String) -> Name = client::name
		val result = client.name("王") { country = "CN" }
		assertTrue(result.known)
		assertEquals(listOf("CN", "TW"), result.countries)
		assertNull(result.gender)
		oldCall("Andrea")
		assertEquals("https://api.parseapi.com/name/%E7%8E%8B?country=CN", stub.requests[0].url)
		assertEquals("https://api.parseapi.com/name/Andrea", stub.requests[1].url)
	}

	@Test fun oldResponsesRemainDecodable() = runBlocking {
		val stub = StubTransport(200, """{"name":"Andrea","valid":true,"gender":null}""")
		val result = ParseAPI("test_key") { transport = stub; retries = 0 }.name("Andrea")
		assertFalse(result.known)
		assertTrue(result.countries.isEmpty())
		assertNull(result.gender)
	}
}
