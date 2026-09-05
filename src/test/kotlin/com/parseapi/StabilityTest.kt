package com.parseapi

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException

private fun stabilityClient(stub: StubTransport): ParseAPI =
	ParseAPI("test") { this.baseUrl = "https://api.parseapi.com"; this.retries = 0; this.transport = stub }

class StabilityTest {
	@Test
	fun emptyExplicitKeyFailsAtConstruction() {
		val error = assertFailsWith<ParseAPIException> { ParseAPI("") }
		assertEquals("missing_api_key", error.code)
		assertEquals(0, error.status)
	}

	@Test
	fun dateEncodingAndCalendarFields() = runBlocking {
		val stub = StubTransport(200, """{"date":"2026-03-04","valid":true,"month_name":"March","week_year":2026,"days_in_month":31,"unix":1772582400,"to":"2026-03-09","days":5}""")
		val result = stabilityClient(stub).date("03/04/2026") { this.format = "mdy"; this.to = "2026-03-09" }
		assertEquals(1, stub.requests.size)
		assertEquals("https://api.parseapi.com/date/03%2F04%2F2026?format=mdy&to=2026-03-09", stub.requests.single().url)
		assertEquals("March", result.monthName)
		assertEquals(2026, result.weekYear)
		assertEquals(31, result.daysInMonth)
		assertEquals(5, result.days)
	}

	@Test
	fun invalidDateAndTodayAreSeparateCalls() = runBlocking {
		val invalid = StubTransport(200, """{"date":"03/04/2026","valid":false,"year":null}""")
		val result = stabilityClient(invalid).date("03/04/2026")
		assertFalse(result.valid)
		assertNull(result.year)
		val today = StubTransport(200, """{"date":"2026-09-05","valid":true}""")
		stabilityClient(today).dateToday { to = "2026-12-25" }
		assertEquals("https://api.parseapi.com/date?to=2026-12-25", today.requests.single().url)
	}

	@Test
	fun blocAndMembers() = runBlocking {
		val bloc = StubTransport(200, """{"bloc":"EU","name":"European Union","members":27}""")
		assertEquals(27, stabilityClient(bloc).bloc("EU").members)
		assertEquals("https://api.parseapi.com/bloc/EU", bloc.requests.single().url)
		val members = StubTransport(200, """{"bloc":"EU","countries":[{"country":"FR","name":"France","calling_code":"33"}]}""")
		assertEquals("33", stabilityClient(members).blocCountries("EU").countries.single().callingCode)
		assertEquals("https://api.parseapi.com/bloc/EU/countries", members.requests.single().url)
	}

	@Test
	fun countryStatesIsOneRequest() = runBlocking {
		val stub = StubTransport(200, """{"country":"US","states":[]}""")
		stabilityClient(stub).countryStates("US")
		assertEquals("https://api.parseapi.com/country/US/states", stub.requests.single().url)
	}

	@Test
	fun nullCoreCollectionsDecodeAsEmpty() = runBlocking {
		val states = StubTransport(200, """{"country":"US","states":null,"future":{"x":1}}""")
		assertTrue(stabilityClient(states).countryStates("US").states.isEmpty())
		val country = StubTransport(200, """{"country":"US","iso3":"USA","numeric":840,"name":"United States","continent":"NA","languages":null,"borders":null}""")
		val result = stabilityClient(country).country("US")
		assertTrue(result.languages.isEmpty())
		assertTrue(result.borders.isEmpty())
	}

	@Test
	fun malformedCollectionAndNullScalarRemainErrors() {
		val malformed = StubTransport(200, """{"country":"US","states":"wrong"}""")
		assertFailsWith<SerializationException> { runBlocking { stabilityClient(malformed).countryStates("US") } }
		assertEquals(1, malformed.requests.size)
		val invalid = StubTransport(200, """{"date":"2026-09-05","valid":null}""")
		assertFailsWith<SerializationException> { runBlocking { stabilityClient(invalid).dateToday() } }
	}

	@Test
	fun largeCurrencyAmountIsNotClampedToLong() = runBlocking {
		val stub = StubTransport(200, """{"base":"USD","quote":"EUR","rate":0.9,"date":"2026-09-04"}""")
		stabilityClient(stub).currencyRate("USD", "EUR") { this.amount = 1e20 }
		assertTrue(stub.requests.single().url.endsWith("amount=1.0E20"))
	}

	@Test
	fun cancellationIsNotRetried() {
		val calls = AtomicInteger()
		val transport = ParseAPITransport {
			calls.incrementAndGet()
			throw CancellationException("lookup cancelled")
		}
		val parse = ParseAPI("test") { this.retries = 2; this.transport = transport }
		assertFailsWith<CancellationException> { runBlocking { parse.domain("example.com") } }
		assertEquals(1, calls.get())
	}

	@Test
	fun defaultTransportDoesNotForwardKeyOnRedirect() {
		val redirected = AtomicInteger()
		val destination = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
		destination.createContext("/") { exchange ->
			redirected.incrementAndGet()
			exchange.sendResponseHeaders(200, -1)
			exchange.close()
		}
		val origin = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
		origin.createContext("/") { exchange ->
			assertEquals("test-secret", exchange.requestHeaders.getFirst("X-API-Key"))
			exchange.responseHeaders.add("Location", "http://127.0.0.1:${destination.address.port}/collect")
			exchange.sendResponseHeaders(302, -1)
			exchange.close()
		}
		destination.start()
		origin.start()
		try {
			val parse = ParseAPI("test-secret") { this.baseUrl = "http://127.0.0.1:${origin.address.port}"; this.retries = 0 }
			val error = assertFailsWith<ParseAPIException> { runBlocking { parse.domain("example.com") } }
			assertEquals(302, error.status)
			assertEquals(0, redirected.get())
		} finally {
			origin.stop(0)
			destination.stop(0)
		}
	}
}
