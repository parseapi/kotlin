package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class NameTest {
	@Test fun formattingLocaleAndNullableResults() = runBlocking {
		val stub = StubTransport(200, """
{"name":"Robert James Smith","valid":true,"gender":"male","salutation":"Mr","short":"R.J. Smith","directory":"Smith, Robert James","initials":"RJS"}
""")
		val client = ParseAPI("test_key") { transport = stub; retries = 0 }
		val result = client.name("Robert James Smith") { country = "US"; deep = true; nameLocale = "en-GB" }
		assertEquals("R.J. Smith", result?.short)
		assertEquals("Smith, Robert James", result?.directory)
		assertEquals("RJS", result?.initials)
		assertEquals("https://api.parseapi.com/name/Robert%20James%20Smith?country=US&deep=true&name_locale=en-GB", stub.requests[0].url)
		client.name("Andrea") { deep = true }
		assertEquals("https://api.parseapi.com/name/Andrea?deep=true", stub.requests[1].url)
		for (body in listOf("""
{"name":"Andrea","valid":true,"short":null,"directory":null,"initials":null}
""", """
{"name":"Andrea","valid":true,"gender":null,"salutation":null}
""", """
{"name":"Andrea","valid":true}
""")) {
			val transport = StubTransport(200, body)
			val nullable = ParseAPI("test_key") { this.transport = transport }.name("Andrea")
			nullable
			assertNull(nullable?.short)
			assertNull(nullable?.directory)
			assertNull(nullable?.initials)
		}
	}

	@Test fun countryAndNullableEvidence() = runBlocking {
		val stub = StubTransport(200, """
{"name":"王","valid":true,"future":true,"gender":null,"salutation":null}
""")
		val client = ParseAPI("test_key") { transport = stub; retries = 0 }
		val oldCall: suspend (String) -> Name = client::name
		val result = client.name("王") { country = "CN"; deep = true }
		result
		assertNull(result?.gender)
		assertNull(result?.salutation)
		oldCall("Andrea")
		assertEquals("https://api.parseapi.com/name/%E7%8E%8B?country=CN&deep=true", stub.requests[0].url)
		assertEquals("https://api.parseapi.com/name/Andrea", stub.requests[1].url)
	}

	@Test fun oldResponsesRemainDecodable() = runBlocking {
		val stub = StubTransport(200, """
{"name":"Andrea","valid":true,"known":true,"gender":null,"future":true}
""")
		val result = ParseAPI("test_key") { transport = stub; retries = 0 }.name("Andrea")
		result
		assertNull(result?.gender)
		assertNull(result?.salutation)
	}
}
