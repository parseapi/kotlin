package com.parseapi

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BankDXTest {
	@Test fun postContextAndRetryKeepRawInputOutOfURL() = runBlocking {
		val stub = StubTransport(mutableListOf(ParseAPIResponse(503,"""{"code":"unavailable"}""",emptyMap()),ParseAPIResponse(200,"""{"valid":true,"deep":{"directory":{"edition":"fixture","country":"DE","match":"future_grain"}}}""",emptyMap())))
		val raw = "89%20\uFEFF00"
		val result = ParseAPI("fixture") { transport = stub }.bank(raw) { country="DE"; deep=true }
		assertEquals("fixture",result.deep?.directory?.edition)
		assertEquals("future_grain",result.deep?.directory?.match)
		assertEquals(2,stub.requests.size)
		for (request in stub.requests) {
			assertEquals("https://api.parseapi.com/bank",request.url)
			assertEquals("POST",request.method)
			assertEquals("application/json",request.headers["Content-Type"])
			assertEquals("2.0.0",request.headers["Parse-Version"])
			val body=Json.parseToJsonElement(request.body!!).jsonObject
			assertEquals(raw,body["iban"]!!.jsonPrimitive.content)
			assertEquals("DE",body["country"]!!.jsonPrimitive.content)
			assertEquals("true",body["deep"]!!.jsonPrimitive.content)
		}
	}

	@Test fun domesticRawBodyOpenCodesAndNullableFields() = runBlocking {
		val stub=StubTransport(200,"""{"valid":false,"routing":null,"account":null,"checks":{"account_checksum":"future_status"},"issues":[{"field":"account","code":"future_issue"}]}""")
		val result=ParseAPI("fixture") {transport=stub}.bankUsAch(BankUsAchInput("021 000021","00a-B %20\uFEFF"))
		assertNull(result.routing); assertNull(result.account)
		assertEquals("future_status",result.checks?.accountChecksum)
		assertEquals("future_issue",result.issues?.first()?.code)
		val request=stub.requests.single()
		assertEquals("POST",request.method); assertEquals("https://api.parseapi.com/bank",request.url)
		val body=Json.parseToJsonElement(request.body!!).jsonObject
		assertEquals(setOf("format","country","routing","account"),body.keys)
		assertEquals("us_ach",body["format"]!!.jsonPrimitive.content)
		assertEquals("US",body["country"]!!.jsonPrimitive.content)
		assertEquals("00a-B %20\uFEFF",body["account"]!!.jsonPrimitive.content)
	}

	@Test fun requirementsCapabilitiesAndDefaultFormat() = runBlocking {
		val stub=StubTransport(200,"""{"country":"US","format":"us_ach","supported":true,"fields":[{"key":"account","label":"Account number","required":true,"type":"string","max_length":17,"max_input_length":128,"length_unit":"non_space_characters"}],"checks":{"account_checksum":"not_supported","future":"future_scope"},"limitations":[]}""")
		val result=ParseAPI("fixture") {transport=stub}.bankRequirements("US","us_ach")
		assertTrue(result.supported); assertEquals(17,result.fields.first().maxLength)
		assertNull(result.fields.first().minLength); assertEquals(128,result.fields.first().maxInputLength)
		assertEquals("future_scope",result.checks["future"])
		assertEquals("https://api.parseapi.com/bank/requirements?country=US&format=us_ach",stub.requests.single().url)
		assertEquals("GET",stub.requests.single().method); assertNull(stub.requests.single().body)
		val unknown=StubTransport(200,"""{"country":"GB","format":"uk_domestic","supported":false,"fields":[],"checks":{},"limitations":[]}""")
		assertFalse(ParseAPI("fixture") {transport=unknown}.bankRequirements("GB","uk_domestic").supported)
		val defaults=StubTransport(200,"""{"country":"DE","format":"iban","supported":true,"fields":[],"checks":{},"limitations":[]}""")
		ParseAPI("fixture") {transport=defaults}.bankRequirements("DE")
		assertEquals("https://api.parseapi.com/bank/requirements?country=DE",defaults.requests.single().url)
	}

	@Test fun nativeHttpTransportWritesBodyAsUtf8() = runBlocking {
		val server=HttpServer.create(InetSocketAddress("127.0.0.1",0),0)
		var method=""; var target=""; var body=""; var version=""
		server.createContext("/bank") { exchange ->
			method=exchange.requestMethod; target=exchange.requestURI.toString()
			body=exchange.requestBody.readBytes().toString(Charsets.UTF_8)
			version=exchange.requestHeaders.getFirst("Parse-Version")
			val response="""{"valid":false}""".toByteArray()
			exchange.sendResponseHeaders(200,response.size.toLong())
			exchange.responseBody.use {it.write(response)}
		}
		server.start()
		try {
			val raw="\uFEFFDE%20 001"
			ParseAPI("fixture") {baseUrl="http://127.0.0.1:${server.address.port}";retries=0}.bank(raw)
			assertEquals("POST",method);assertEquals("/bank",target);assertEquals("2.0.0",version)
			assertEquals(raw,Json.parseToJsonElement(body).jsonObject["iban"]!!.jsonPrimitive.content)
		} finally {server.stop(0)}
	}
}
