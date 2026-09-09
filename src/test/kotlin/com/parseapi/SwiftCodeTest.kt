package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class SwiftCodeTest {
	private fun client(stub: StubTransport) = ParseAPI("test_key") { transport = stub; retries = 0; baseUrl = "https://api.parseapi.com" }

	@Test fun swiftEncodesInputAndToleratesNewFields() = runBlocking {
		val stub = StubTransport(200, """{"swift":"CHASUS33","valid":true,"country":"US","name":"JPMORGAN CHASE BANK, N.A.","future":true}""")
		val record: SwiftCode = client(stub).swift("CHAS/US33 ?#")
		assertEquals("https://api.parseapi.com/swift/CHAS%2FUS33%20%3F%23", stub.requests[0].url)
		assertTrue(record.valid)
		assertEquals("US", record.country)
		assertEquals("JPMORGAN CHASE BANK, N.A.", record.name)
	}

	@Test fun validSyntaxDoesNotRequireKnownName() = runBlocking {
		val stub = StubTransport(200, """{"swift":"ZZZZZZ99","valid":true,"country":"ZZ","name":null,"future":{}}""")
		val record = client(stub).swift("ZZZZZZ99")
		assertTrue(record.valid)
		assertEquals("ZZ", record.country)
		assertNull(record.name)
	}

	@Test fun junkIsDataAndAbsentOptionalFieldsStayUnknown() = runBlocking {
		for (body in listOf("""{"swift":"JUNK","valid":false,"country":null,"name":null}""", """{"swift":"JUNK","valid":false}""")) {
			val record = client(StubTransport(200, body)).swift("JUNK")
			assertFalse(record.valid)
			assertNull(record.country)
			assertNull(record.name)
		}
	}
}
