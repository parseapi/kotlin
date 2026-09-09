package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking

class BinTest {
	private fun client(stub: StubTransport) = ParseAPI("test_key") { transport = stub; retries = 0; baseUrl = "https://api.parseapi.com" }
	@Test fun preservesPrefixAndFalse(): Unit = runBlocking {
		val stub = StubTransport(200, """{"bin":"00123456","prefix":"001234","country":null,"issuer":"Fixture Bank","brand":"future-brand","type":null,"prepaid":false,"deep":{},"future":true}""")
		val record = client(stub).bin("00 1234-56") { deep = true }
		assertEquals("https://api.parseapi.com/bin/00%201234-56?deep=true", stub.requests[0].url)
		assertEquals("00123456", record.bin)
		assertEquals("001234", record.prefix)
		assertNull(record.country)
		assertEquals(false, record.prepaid)
		assertNotNull(record.deep)
	}
	@Test fun unknownAndAbsentFieldsStayUnknown() = runBlocking {
		for (body in listOf("""{"bin":"000000","prefix":null,"country":null,"issuer":null,"brand":null,"type":null,"prepaid":null}""", """{"bin":"000000"}""")) {
			val stub = StubTransport(200, body)
			val record = client(stub).bin("000000")
			assertNull(record.prefix)
			assertNull(record.prepaid)
			assertNull(record.deep)
			assertEquals("https://api.parseapi.com/bin/000000", stub.requests[0].url)
		}
	}
}
