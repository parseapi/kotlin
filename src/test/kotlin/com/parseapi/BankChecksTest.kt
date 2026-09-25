package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BankChecksTest {
	@Test fun rawInputReachesTransport() = runBlocking {
		for ((input, _) in listOf(
			"DE89.370400440532013000" to "DE89.370400440532013000",
			"\uFEFFDE89370400440532013000" to "%EF%BB%BFDE89370400440532013000",
			"DE89\u00A0370400440532013000" to "DE89%C2%A0370400440532013000",
			"DE89%20370400440532013000" to "DE89%2520370400440532013000",
		)) {
			val stub = StubTransport(200, """{"valid":false}""")
			ParseAPI("fixture") { transport = stub }.bank(input)
			assertEquals("https://api.parseapi.com/bank", stub.requests[0].url)
			assertEquals("POST", stub.requests[0].method)
			assertEquals(input, Json.parseToJsonElement(stub.requests[0].body!!).jsonObject["iban"]!!.jsonPrimitive.content)
			assertEquals("2.0.0", stub.requests[0].headers["Parse-Version"])
		}
	}

	@Test fun oldAndNullResponses() = runBlocking {
		for (extra in listOf("", """, "checks":null,"issues":null""")) {
			val body = """{"iban":"DE89370400440532013000","valid":true,"deep":{"account":"0532013000"}""" + extra + "}"
			val result = ParseAPI("fixture") { transport = StubTransport(200, body) }.bank("DE89370400440532013000")
			assertNull(result.checks)
			assertNull(result.issues)
			assertEquals("DE89370400440532013000", result.iban)
			assertEquals("0532013000", result.deep?.account)
		}
	}

	@Test fun passedAndUnsupportedChecks() = runBlocking {
		val body = """{"iban":"DE89370400440532013000","valid":true,"checks":{"input":"passed","country":"passed","length":"passed","structure":"passed","checksum":"passed","national":"not_supported"},"issues":[]}"""
		val stub = StubTransport(200, body)
		val result = ParseAPI("fixture") { transport = stub }.bank("DE89 3704 0044 0532 0130 00")
		val checks = assertNotNull(result.checks)
		for (status in listOf(checks.input, checks.country, checks.length, checks.structure, checks.checksum)) assertEquals("passed", status)
		assertEquals("not_supported", checks.national)
		assertTrue(result.valid)
		assertEquals(0, assertNotNull(result.issues).size)
		assertEquals("https://api.parseapi.com/bank", stub.requests[0].url)
		assertEquals("2.0.0", stub.requests[0].headers["Parse-Version"])
	}

	@Test fun nationalFailureAndFutureCodes() = runBlocking {
		for ((status, code) in listOf("failed" to "invalid_national_checksum", "future_status" to "future_issue")) {
			val body = """{"valid":false,"checks":{"checksum":"passed","national":"$status","future":true},"issues":[{"field":"iban","code":"$code","message":"Review these bank details.","future":true}],"future":true}"""
			val result = ParseAPI("fixture") { transport = StubTransport(200, body) }.bank("fixture")
			assertFalse(result.valid)
			assertEquals(status, result.checks?.national)
			assertNull(result.checks?.input)
			val issue = assertNotNull(result.issues).single()
			assertEquals("iban", issue.field)
			assertEquals(code, issue.code)
			assertEquals("Review these bank details.", issue.message)
		}
		val empty = ParseAPI("fixture") { transport = StubTransport(200, """{"valid":false,"checks":{},"issues":[{}]}""") }.bank("fixture")
		assertNull(empty.checks?.national)
		assertNull(empty.issues?.first()?.code)
	}
}
