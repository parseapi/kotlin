package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class APIVersionTest {
	@Test
	fun contractPinSurvivesRetriesAndSelfLookup() = runBlocking {
		val stub = StubTransport(mutableListOf(
			ParseAPIResponse(503, """{"code":"unavailable","message":"Try again"}""", mapOf("retry-after" to "0")),
			ParseAPIResponse(200, """{"ip":"192.0.2.1","country":null,"deep":{"datacenter":null},"future":true}""", emptyMap()),
		))
		val client = ParseAPI("parse_app_version_test") {
			appId = "com.example.version"
			baseUrl = "https://api.parseapi.com"
			retries = 1
			transport = stub
		}
		val result = client.ipSelf { deep = true }
		assertEquals("192.0.2.1", result.ip)
		assertNull(result.country)
		assertNotNull(result.deep)
		assertNull(result.deep?.datacenter)
		assertEquals(2, stub.requests.size)
		for (request in stub.requests) {
			assertEquals("https://api.parseapi.com/ip?deep=true", request.url)
			assertEquals("2.0.0", request.headers["Parse-Version"])
			assertEquals("parse_app_version_test", request.headers["X-API-Key"])
			assertEquals("com.example.version", request.headers["X-App-Id"])
			assertEquals("parseapi-kotlin/${ParseAPI.VERSION}", request.headers["User-Agent"])
		}
	}

	@Test
	fun contractPinWithUserAgentInput() = runBlocking {
		val stub = StubTransport(200, """{"useragent":"Example/1.0","bot":false,"mobile":false,"deep":{}}""")
		val client = ParseAPI("parse_app_version_test") {
			appId = "com.example.version"
			transport = stub
		}
		client.useragent("Example/1.0")
		val request = stub.requests.single()
		assertEquals("2.0.0", request.headers["Parse-Version"])
		assertEquals("Example/1.0", request.headers["User-Agent"])
		assertEquals("parse_app_version_test", request.headers["X-API-Key"])
		assertEquals("com.example.version", request.headers["X-App-Id"])
	}

	@Test
	fun versionErrorsDoNotRetryOrFallBack() = runBlocking {
		for (status in listOf(400, 410)) {
			val stub = StubTransport(status, """{"code":"invalid_request","message":"Unsupported API version","request_id":"req_version"}""")
			val client = ParseAPI("parse_app_version_test") { retries = 2; transport = stub }
			val error = assertFailsWith<ParseAPIException> { client.ipSelf() }
			assertEquals(status, error.status)
			assertEquals("invalid_request", error.code)
			assertEquals("req_version", error.requestId)
			assertEquals(1, stub.requests.size)
			assertEquals("2.0.0", stub.requests.single().headers["Parse-Version"])
		}
	}
}
