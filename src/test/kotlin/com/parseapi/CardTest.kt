package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class CardTest {
	private fun client(stub: StubTransport) = ParseAPI("test_key") { transport = stub; retries = 0; baseUrl = "https://api.parseapi.com" }
	@Test fun preservesPrefixAndFalse(): Unit = runBlocking {
		val stub = StubTransport(200, """{"bin":"00123456","brand":"future-brand","brand_name":null,"logo":"https://cdn.parseapi.com/card/generic.svg","deep":{"prefix":"001234","issuer":"Fixture Bank","country":null,"type":null,"prepaid":false},"future":true}""")
		val record = client(stub).card("00 1234-56") { deep = true }
		assertEquals("https://api.parseapi.com/card/00%201234-56?deep=true", stub.requests[0].url)
		assertEquals("00123456", record.bin)
		assertEquals("001234", record.deep?.prefix)
		assertNull(record.deep?.country)
		assertEquals(false, record.deep?.prepaid)
	}
	@Test fun unknownAndAbsentFieldsStayUnknown() = runBlocking {
		for (body in listOf("""{"bin":"000000","brand":null,"brand_name":null,"logo":"https://cdn.parseapi.com/card/generic.svg"}""", """{"bin":"000000","logo":"https://cdn.parseapi.com/card/generic.svg"}""")) {
			val stub = StubTransport(200, body)
			val record = client(stub).card("000000")
			assertNull(record.deep?.prefix)
			assertNull(record.deep?.prepaid)
			assertEquals("https://api.parseapi.com/card/000000", stub.requests[0].url)
		}
	}
}
