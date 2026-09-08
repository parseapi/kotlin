package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DnsTest {
	@Test fun recordsAndQuestion() = runBlocking {
		val stub = StubTransport(mutableListOf(
			ParseAPIResponse(200, """{"domain":"example.com","records":[{"name":"example.com.","type":"TXT","ttl":0,"value":"\"one\" \"two\"","future":true},{"name":"alias.example.","type":"CNAME","ttl":300,"value":"target.example."}],"future":true}""", emptyMap()),
			ParseAPIResponse(200, """{"domain":"example.com","records":[]}""", emptyMap())
		))
		val client = ParseAPI("test_key") { transport = stub; retries = 0 }
		val result = client.dns("_dmarc.bücher.example.") { type = "txt" }
		assertEquals(2, result.records.size)
		assertEquals(0L, result.records[0].ttl)
		assertEquals("\"one\" \"two\"", result.records[0].value)
		assertEquals("CNAME", result.records[1].type)
		assertTrue(client.dns("example.com").records.isEmpty())
		assertEquals("https://api.parseapi.com/dns/_dmarc.b%C3%BCcher.example.?type=txt", stub.requests[0].url)
		assertEquals("https://api.parseapi.com/dns/example.com", stub.requests[1].url)
	}
}
