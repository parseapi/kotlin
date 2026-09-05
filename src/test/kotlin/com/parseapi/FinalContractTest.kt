package com.parseapi

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*
import kotlinx.coroutines.*

class FinalContractTest {
    @Test
    fun junkAddressAndCompanyCanEchoNull() = runBlocking {
        val address = StubTransport(200, """{"address":null,"valid":false}""")
        val a = ParseAPI("test") { transport = address }.address(" ")
        assertNull(a.address)
        assertFalse(a.valid)
        val company = StubTransport(200, """{"company":null,"valid":false}""")
        val c = ParseAPI("test") { transport = company }.company(" ")
        assertNull(c.company)
        assertFalse(c.valid)
    }

    @Test
    fun meteredDefaultsDoNotRetryAndExplicitOverrideDoes() = runBlocking {
        val calls: List<suspend (ParseAPI) -> Any> = listOf(
            { it.carrier("junk") }, { it.caller("junk") }, { it.hlr("junk") },
            { it.address("123 Main St") { deep = true } }, { it.email("a@example.com") { deep = true } }, { it.vat("DE136695976") { deep = true } },
        )
        for (call in calls) {
            val stub = StubTransport(503, "{}")
            val parse = ParseAPI("test") { transport = stub }
            assertFailsWith<ParseAPIException> { call(parse) }
            assertEquals(1, stub.requests.size)
        }
        val retryStub = StubTransport(mutableListOf(
            ParseAPIResponse(503, "{}", mapOf("retry-after" to "0")),
            ParseAPIResponse(200, """{"phone":"junk","valid":false}""", emptyMap()),
        ))
        ParseAPI("test") { retries = 1; transport = retryStub }.carrier("junk")
        assertEquals(2, retryStub.requests.size)
    }

    @Test
    fun ordinaryLookupRetainsTwoRetries() = runBlocking {
        val stub = StubTransport(mutableListOf(
            ParseAPIResponse(503, "{}", mapOf("retry-after" to "0")),
            ParseAPIResponse(503, "{}", mapOf("retry-after" to "0")),
            ParseAPIResponse(200, """{"email":"a@example.com","valid":true,"role":false,"disposable":false}""", emptyMap()),
        ))
        ParseAPI("test") { transport = stub }.email("a@example.com")
        assertEquals(3, stub.requests.size)
    }

    @Test
    fun retryAfterAcceptsHttpDatesAndCapsDelay() {
        val parse = ParseAPI("test")
        assertEquals(0, parse.retryDelayMs(0, "Sun, 06 Nov 1994 08:49:37 GMT"))
        assertEquals(0, parse.retryDelayMs(0, "Sunday, 06-Nov-94 08:49:37 GMT"))
        assertEquals(0, parse.retryDelayMs(0, "Sun Nov 6 08:49:37 1994"))
        val future = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US).format(java.util.Date(System.currentTimeMillis() + 60_000))
        assertEquals(5000, parse.retryDelayMs(0, future))
        assertTrue(parse.retryDelayMs(999, "NaN") in 0..5000)
    }

    @Test
    fun invalidConfigurationFailsBeforeTransport() {
        assertFailsWith<IllegalArgumentException> { ParseAPI("test") { timeoutMs = 0 } }
        assertFailsWith<IllegalArgumentException> { ParseAPI("test") { retries = -1 } }
        for (url in listOf("", "relative/path", "ftp://example.com", "https://user:pass@example.com", "https://example.com?q=1", "https://example.com#x")) {
            assertFailsWith<IllegalArgumentException> { ParseAPI("test") { baseUrl = url } }
        }
    }

    @Test
    fun companyAddressAndSearchDecodeWithoutExtraRequests() = runBlocking {
        val address = StubTransport(200, """{"address":"123 Main St","valid":true,"registered":false,"country":"US","deep":{}}""")
        val a = ParseAPI("test") { transport = address }.address("123 Main St") { country = "US"; deep = true }
        assertTrue(a.valid)
        assertNotNull(a.deep)
        assertTrue(address.requests.single().url.endsWith("/address/123%20Main%20St?country=US&deep=true"))
        val search = StubTransport(200, """{"q":"123 Main","addresses":[{"address":"123 Main St","postal":"28202"}]}""")
        val hits = ParseAPI("test") { transport = search }.addressSearch("123 Main") { postal = "28202"; country = "US" }
        assertEquals("28202", hits.addresses.single().postal)
        assertTrue(search.requests.single().url.contains("q=123%20Main&country=US&postal=28202"))
        val company = StubTransport(200, """{"company":"01234567","valid":true,"registered":true,"deep":{"country":{"name":"United Kingdom","blocs":null,"tax":"VAT"}}}""")
        val c = ParseAPI("test") { transport = company }.company("01234567") { country = "GB"; deep = true }
        assertTrue(c.deep!!.country!!.blocs.isEmpty())
        assertTrue(company.requests.single().url.endsWith("/company/01234567?country=GB&deep=true"))
    }

    @Test
    fun fullWeatherAndTimezoneFieldsAreAccessible() = runBlocking {
        val weather = StubTransport(200, """{"latitude":40,"longitude":-74,"current":{},"source":{"id":"example","name":"Example"},"deep":{"minutes":[{"at":"2026-09-05T12:00Z","precipitation":0.2}],"hours":[{"at":"2026-09-05T12:00Z","feels_like":21,"wind_gust":30}],"days":[{"date":"2026-09-06","high":25}],"air":{"pm2_5":7.5},"history":{"date":"2026-09-01","high_f":80}}}""")
        val result = ParseAPI("test") { transport = weather }.weather(40.0, -74.0) { deep = true; date = "2026-09-01" }
        assertEquals(21.0, result.deep!!.hours!!.single().feelsLike)
        assertEquals(30.0, result.deep!!.hours!!.single().windGust)
        assertEquals(7.5, result.deep!!.air!!.pm25)
        assertEquals(80.0, result.deep!!.history!!.highF)
        assertTrue(weather.requests.single().url.contains("date=2026-09-01"))
        val timezone = StubTransport(200, """{"timezone":"America/New_York","at":"2026-09-05T09:00:00-04:00","to":{"timezone":"Europe/London","offset":"+01:00","offset_minutes":60,"dst":true,"at":"2026-09-05T14:00:00+01:00"}}""")
        val zone = ParseAPI("test") { transport = timezone }.timezone("America/New_York") { at = "2026-09-05T09:00:00"; to = "Europe/London" }
        assertEquals("2026-09-05T14:00:00+01:00", zone.to!!.at)
        assertEquals(60, zone.to!!.offsetMinutes)
        assertTrue(timezone.requests.single().url.endsWith("to=Europe%2FLondon"))
    }

    @Test
    fun responseObjectsHaveNoPublicConstructionCopyOrComponents() {
        for (type in listOf(Country::class.java, DateInfo::class.java, Weather::class.java, Company::class.java)) {
            assertTrue(type.declaredConstructors.filterNot { it.isSynthetic }.none { java.lang.reflect.Modifier.isPublic(it.modifiers) })
            assertTrue(type.methods.none { it.name == "copy" || it.name.matches(Regex("component\\d+")) })
        }
    }

    @Test
    fun cancellingDefaultTransportStopsWaitingDuringSocketRead() = runBlocking {
        val entered = CompletableDeferred<Unit>()
        val release = CountDownLatch(1)
        val calls = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            calls.incrementAndGet()
            try {
                exchange.sendResponseHeaders(200, 1000)
                exchange.responseBody.write('{'.code)
                exchange.responseBody.flush()
                entered.complete(Unit)
                release.await(5, TimeUnit.SECONDS)
            } finally { exchange.close() }
        }
        server.start()
        try {
            val parse = ParseAPI("test") { baseUrl = "http://127.0.0.1:${server.address.port}"; timeoutMs = 10_000 }
            val lookup = launch { parse.domain("example.com") }
            withTimeout(3000) { entered.await() }
            withTimeout(1000) { lookup.cancelAndJoin() }
            assertEquals(1, calls.get())
        } finally { release.countDown(); server.stop(0) }
    }
}
