package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class EmailEnrichmentTest {
	@Test fun preservesFalseAndFutureCodes() = runBlocking {
		val stub = StubTransport(200, """
{"email":"jane.doe+news@example.com","valid":true,"role":false,"disposable":false,"future":true,"first_name":"Jane","no_reply":false,"tag":"news","mail_provider":"future-provider","deliverable":true,"catchall":false,"status":"future-status","reason":"future_reason"}
""")
		val result = ParseAPI("fixture") { transport = stub }.email("jane.doe+news@example.com") { deep = true }
		assertEquals("Jane", result?.firstName)
		assertEquals(false, result?.noReply)
		assertEquals("news", result?.tag)
		assertEquals("future-provider", result?.mailProvider)
		assertEquals("future-status", result?.status)
		assertEquals("future_reason", result?.reason)
	}

	@Test fun oldNullAndLockedResults() = runBlocking {
		for (extra in listOf("", """, "deep": {}""", """, "deep": {"first_name": null, "no_reply": null, "tag": null, "mail_provider": null, "status": null, "reason": null}""", """, "deep": {"deliverable": false, "catchall": true}""")) {
			val body = """{"email":"a@example.com","valid":true,"role":false,"disposable":false""" + extra + "}"
			val stub = StubTransport(200, body)
			val result = ParseAPI("fixture") { transport = stub }.email("a@example.com")
			assertNull(result?.firstName)
			assertNull(result?.noReply)
			assertNull(result?.tag)
			assertNull(result?.mailProvider)
			assertNull(result?.status)
			assertNull(result?.reason)
			if (extra.isEmpty()) else result
		}
	}
}
