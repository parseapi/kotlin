package com.parseapi

import kotlin.test.*
import kotlinx.coroutines.*

class CardDXTest {
    @Test fun invalidPrefixesNeverDispatchAndAcceptedInputIsPreserved() = runBlocking {
        val stub = StubTransport(200, """{"bin":"001234","logo":"https://cdn.parseapi.com/card/generic.svg"}""")
        val parse = ParseAPI("fixture") { transport = stub }
        for (raw in listOf("4111111111111111", "4111-1111-1111-1111", "1", "123456789012", "１２３４５６", "001\u00a0234", "001\u200b234", "00%20234", "001\u000b234", " ".repeat(59) + "001234")) {
            val error = assertFailsWith<IllegalArgumentException> { parse.card(raw) }
            assertEquals("Card requires a 2-11 digit prefix string.", error.message)
        }
        assertTrue(stub.requests.isEmpty())
        for (raw in listOf(" \t00-1234\r\n", " ".repeat(58) + "001234", "12345678901")) {
            parse.card(raw)
            assertTrue(stub.requests.last().url.endsWith("/card/${enc(raw)}"))
        }
    }

    @Test fun retryAfterBudgetReturnsOriginalErrorAndMetadata() = runBlocking {
        val format = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US)
        val future = format.format(java.util.Date(System.currentTimeMillis() + 60_000))
        for (header in listOf("60", "9".repeat(400), future)) {
            val stub = StubTransport(mutableListOf(ParseAPIResponse(429, """{"code":"rate_limited","message":"Later","request_id":"receipt"}""", mapOf("retry-after" to header))))
            val parse = ParseAPI("fixture") { transport = stub }
            val error = withTimeout(1000) { assertFailsWith<ParseAPIException> { parse.country("US") } }
            assertEquals(header, error.retryAfter)
            assertEquals("rate_limited", error.code)
            assertEquals("receipt", error.requestId)
            assertEquals(1, stub.requests.size)
        }
        for (header in listOf("0", "0.01", "invalid")) {
            val stub = StubTransport(mutableListOf(ParseAPIResponse(503, "{}", mapOf("retry-after" to header)), ParseAPIResponse(503, "{}", mapOf("retry-after" to header))))
            val parse = ParseAPI("fixture") { transport = stub; retries = 1 }
            val error = assertFailsWith<ParseAPIException> { parse.country("US") }
            assertEquals(header, error.retryAfter)
            assertEquals(2, stub.requests.size)
        }
        val parse = ParseAPI("fixture")
        assertEquals(0L, parse.retryDelayMs(0, "0"))
        assertEquals(10L, parse.retryDelayMs(0, "0.01"))
        assertEquals(1L, parse.retryDelayMs(0, "0.0001"))
        assertNull(parse.retryDelayMs(0, "9".repeat(400)))
        assertTrue(parse.retryDelayMs(999, "NaN")!! <= 5000)
        assertNull(ParseAPIException(400, "test", "test", null, null).retryAfter)
    }
}
