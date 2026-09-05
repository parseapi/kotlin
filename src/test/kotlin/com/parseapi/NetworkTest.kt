package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class NetworkTest {
	private fun client(stub: StubTransport) = ParseAPI("test_key") { transport = stub; retries = 0; baseUrl = "https://api.parseapi.com" }

	@Test fun asnPreservesNullsAndFullNumberRange() = runBlocking {
		val stub = StubTransport(200, """{"asn":4294967295,"name":null,"country":null,"country_name":null,"future":true}""")
		val record = client(stub).asn("AS4294967295")
		assertEquals("https://api.parseapi.com/asn/AS4294967295", stub.requests[0].url)
		assertEquals(4294967295L, record.asn)
		assertNull(record.name)
		assertNull(record.country)
		assertNull(record.countryName)
	}

	@Test fun macEncodesColonsAndDecodesFlags() = runBlocking {
		val stub = StubTransport(200, """{"mac":"02:00:00:00:00:01","valid":true,"vendor":null,"local":true,"multicast":false,"future":true}""")
		val record = client(stub).mac("02:00:00:00:00:01")
		assertEquals("https://api.parseapi.com/mac/02%3A00%3A00%3A00%3A00%3A01", stub.requests[0].url)
		assertTrue(record.valid)
		assertEquals(true, record.local)
		assertEquals(false, record.multicast)
		assertNull(record.vendor)
	}

	@Test fun invalidMacIsData() = runBlocking {
		val stub = StubTransport(200, """{"mac":"junk","valid":false,"vendor":null,"local":null,"multicast":null}""")
		val record = client(stub).mac("junk")
		assertEquals("junk", record.mac)
		assertFalse(record.valid)
		assertNull(record.vendor)
		assertNull(record.local)
		assertNull(record.multicast)
	}
}
