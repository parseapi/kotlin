package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class EmailEnrichmentTest {
	@Test fun preservesFalseAndFutureCodes() = runBlocking {
		val stub = StubTransport(200, """{"email":"jane.doe+news@example.com","valid":true,"role":false,"disposable":false,"deep": {"first_name":"Jane","no_reply":false,"tag":"news","mail_provider":"future-provider","deliverable":true,"catchall":false,"status":"future-status","reason":"future_reason"},"future":true}""")
		val result = ParseAPI("fixture") { transport = stub }.email("jane.doe+news@example.com") { deep = true }
		assertEquals("Jane", result.deep?.firstName)
		assertEquals(false, result.deep?.noReply)
		assertEquals("news", result.deep?.tag)
		assertEquals("future-provider", result.deep?.mailProvider)
		assertEquals("future-status", result.deep?.status)
		assertEquals("future_reason", result.deep?.reason)
	}

	@Test fun oldNullAndLockedResults() = runBlocking {
		for (extra in listOf("", """, "deep": {}""", """, "deep": {"first_name": null, "no_reply": null, "tag": null, "mail_provider": null, "status": null, "reason": null}""", """, "deep": {"deliverable": false, "catchall": true}""")) {
			val body = """{"email":"a@example.com","valid":true,"role":false,"disposable":false""" + extra + "}"
			val stub = StubTransport(200, body)
			val result = ParseAPI("fixture") { transport = stub }.email("a@example.com")
			assertNull(result.deep?.firstName)
			assertNull(result.deep?.noReply)
			assertNull(result.deep?.tag)
			assertNull(result.deep?.mailProvider)
			assertNull(result.deep?.status)
			assertNull(result.deep?.reason)
			if (extra.isEmpty()) assertNull(result.deep) else assertNotNull(result.deep)
		}
	}
}
