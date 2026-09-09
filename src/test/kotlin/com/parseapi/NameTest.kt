package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NameTest {
	@Test fun countryAndMembershipAreAdditive() = runBlocking {
		val stub = StubTransport(200, """{"name":"王","valid":true,"future":true,"deep":{"known":true,"countries":["CN","TW"],"gender":null}}""")
		val client = ParseAPI("test_key") { transport = stub; retries = 0 }
		val oldCall: suspend (String) -> Name = client::name
		val result = client.name("王") { country = "CN"; deep = true }
		assertEquals(true, result.deep?.known)
		assertEquals(listOf("CN", "TW"), result.deep?.countries)
		assertNull(result.deep?.gender)
		oldCall("Andrea")
		assertEquals("https://api.parseapi.com/name/%E7%8E%8B?country=CN&deep=true", stub.requests[0].url)
		assertEquals("https://api.parseapi.com/name/Andrea", stub.requests[1].url)
	}

	@Test fun oldResponsesRemainDecodable() = runBlocking {
		val stub = StubTransport(200, """{"name":"Andrea","valid":true,"deep":{"gender":null}}""")
		val result = ParseAPI("test_key") { transport = stub; retries = 0 }.name("Andrea")
		assertNull(result.deep?.known)
		assertNull(result.deep?.countries)
		assertNull(result.deep?.gender)
	}
}
