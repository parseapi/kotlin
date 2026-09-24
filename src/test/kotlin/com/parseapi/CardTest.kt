package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class CardTest {
	private fun client(stub: StubTransport) = ParseAPI("test_key") { transport = stub; retries = 0; baseUrl = "https://api.parseapi.com" }
	@Test fun preservesPrefixAndFalse(): Unit = runBlocking {
		val stub = StubTransport(200, """{"bin":"00123456","prefix":"001234","country":null,"issuer":"Fixture Bank","brand":"future-brand","type":null,"prepaid":false,"deep":{},"future":true}""")
		val record = client(stub).card("00 1234-56")
		assertEquals("https://api.parseapi.com/card/00%201234-56", stub.requests[0].url)
		assertEquals("00123456", record.bin)
		assertEquals("001234", record.prefix)
		assertNull(record.country)
		assertEquals(false, record.prepaid)
	}
	@Test fun unknownAndAbsentFieldsStayUnknown() = runBlocking {
		for (body in listOf("""{"bin":"000000","prefix":null,"country":null,"issuer":null,"brand":null,"type":null,"prepaid":null}""", """{"bin":"000000"}""")) {
			val stub = StubTransport(200, body)
			val record = client(stub).card("000000")
			assertNull(record.prefix)
			assertNull(record.prepaid)
			assertEquals("https://api.parseapi.com/card/000000", stub.requests[0].url)
		}
	}
}
