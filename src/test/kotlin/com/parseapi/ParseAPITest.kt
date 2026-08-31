package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class StubTransport(
	private val responses: MutableList<ParseAPIResponse> = mutableListOf(ParseAPIResponse(200, "{}", emptyMap())),
) : ParseAPITransport {
	val requests = mutableListOf<ParseAPIRequest>()

	constructor(status: Int, body: String) : this(mutableListOf(ParseAPIResponse(status, body, emptyMap())))

	@Synchronized
	override fun execute(request: ParseAPIRequest): ParseAPIResponse {
		requests.add(request)
		return if (responses.size > 1) responses.removeFirst() else responses[0]
	}
}

private fun client(
	stub: StubTransport,
	appId: String? = null,
	retries: Int = 0,
): ParseAPI = ParseAPI(
	key = "parse_testtesttesttest",
	appId = appId,
	baseUrl = "https://api.parseapi.com",
	retries = retries,
	transport = stub,
)

private const val IP_BODY = """{"ip":"8.8.8.8","country":null,"country_name":null,"continent":null,"asn":null,"asn_name":null}"""
private const val DOMAIN_BODY = """{"domain":"x.com","available":false}"""

class UrlMappingTest {
	@Test
	fun ipPath() = runBlocking {
		val stub = StubTransport(200, IP_BODY)
		client(stub).ip("8.8.8.8")
		assertEquals("https://api.parseapi.com/ip/8.8.8.8", stub.requests[0].url)
	}

	@Test
	fun ipSelfBarePath() = runBlocking {
		val stub = StubTransport(200, IP_BODY)
		client(stub).ipSelf()
		assertEquals("https://api.parseapi.com/ip", stub.requests[0].url)
	}

	@Test
	fun timezoneEncodesSlash() = runBlocking {
		val stub = StubTransport(200, """{"timezone":"America/New_York"}""")
		client(stub).timezone("America/New_York")
		assertEquals("https://api.parseapi.com/timezone/America%2FNew_York", stub.requests[0].url)
	}

	@Test
	fun emailEncodesAt() = runBlocking {
		val stub = StubTransport(200, """{"email":"a@b.com","valid":true,"role":false,"disposable":false}""")
		client(stub).email("a@b.com")
		assertEquals("https://api.parseapi.com/email/a%40b.com", stub.requests[0].url)
	}

	@Test
	fun vatFromDeep() = runBlocking {
		val stub = StubTransport(200, """{"vat":"DE136695976","valid":true,"country":"DE"}""")
		client(stub).vat("DE136695976", from = "IE6388047V", deep = true)
		assertEquals("https://api.parseapi.com/vat/DE136695976?from=IE6388047V&deep=true", stub.requests[0].url)
	}

	@Test
	fun ibanCountry() = runBlocking {
		val stub = StubTransport(200, """{"iban":"DE89370400440532013000","valid":true,"country":"DE","checksum":"89","bank":"37040044","branch":null,"account":"0532013000"}""")
		client(stub).iban("89370400440532013000", country = "DE")
		assertEquals("https://api.parseapi.com/iban/89370400440532013000?country=DE", stub.requests[0].url)
	}

	@Test
	fun npi() = runBlocking {
		val stub = StubTransport(200, """{"npi":"1881018208","valid":true,"registered":true,"type":"organization","name":"Mayo Clinic"}""")
		val record = client(stub).npi("1881018208")
		assertEquals("https://api.parseapi.com/npi/1881018208", stub.requests[0].url)
		assertEquals(true, record.registered)
	}

	@Test
	fun phoneEncodesPlus() = runBlocking {
		val stub = StubTransport(200, """{"phone":"+14155552671","valid":true,"country":"US"}""")
		client(stub).phone("+14155552671")
		assertEquals("https://api.parseapi.com/phone/%2B14155552671", stub.requests[0].url)
	}

	@Test
	fun stateSendsCountry() = runBlocking {
		val stub = StubTransport(200, """{"state":"NC","name":"North Carolina","country":"US"}""")
		client(stub).state("NC", country = "US")
		assertEquals("https://api.parseapi.com/state/NC?country=US", stub.requests[0].url)
	}

	@Test
	fun citySearchQuery() = runBlocking {
		val stub = StubTransport(200, """{"q":"char","cities":[]}""")
		client(stub).citySearch("char", country = "US", limit = 5)
		assertEquals("https://api.parseapi.com/city?q=char&country=US&limit=5", stub.requests[0].url)
	}

	@Test
	fun cityIdPath() = runBlocking {
		val stub = StubTransport(200, """{"name":"Charlotte","country":"US","id":"city_abcdefabcdef"}""")
		client(stub).cityId("city_abcdefabcdef")
		assertEquals("https://api.parseapi.com/city/id/city_abcdefabcdef", stub.requests[0].url)
	}

	@Test
	fun pointCoordsAndDeep() = runBlocking {
		val stub = StubTransport(200, """{"latitude":36.07,"longitude":-79.79,"deep":{}}""")
		client(stub).point(36.07, -79.79, deep = true)
		assertEquals("https://api.parseapi.com/point?lat=36.07&lon=-79.79&deep=true", stub.requests[0].url)
	}

	@Test
	fun deepOmittedWhenFalse() = runBlocking {
		val stub = StubTransport(200, IP_BODY)
		client(stub).ip("8.8.8.8", deep = false)
		assertEquals("https://api.parseapi.com/ip/8.8.8.8", stub.requests[0].url)
	}

	@Test
	fun wholeNumberCoordsStayClean() = runBlocking {
		val stub = StubTransport(200, """{"latitude":40,"longitude":-74}""")
		client(stub).elevation(40.0, -74.0)
		assertEquals("https://api.parseapi.com/elevation?lat=40&lon=-74", stub.requests[0].url)
	}

	@Test
	fun stateByNameOmitsCountry() = runBlocking {
		val stub = StubTransport(200, """{"state":"CO","name":"Colorado","country":"US"}""")
		client(stub).state("colorado")
		assertEquals("https://api.parseapi.com/state/colorado", stub.requests[0].url)
	}

	@Test
	fun cityNearbyPath() = runBlocking {
		val stub = StubTransport(
			200,
			"""{"city":"Denver","country":"US","radius":8,"unit":"mi","nearby":[]}""",
		)
		client(stub).cityNearby("denver", radius = 8.0, unit = "mi", limit = 3)
		assertEquals("https://api.parseapi.com/city/denver/nearby?radius=8&unit=mi&limit=3", stub.requests[0].url)
	}

	@Test
	fun postalBareOmitsCountry() = runBlocking {
		val stub = StubTransport(200, """{"postal":"SW1A 1AA","country":"GB"}""")
		client(stub).postal("SW1A 1AA")
		assertEquals("https://api.parseapi.com/postal/SW1A%201AA", stub.requests[0].url)
	}

	@Test
	fun postalDistancePath() = runBlocking {
		val stub = StubTransport(
			200,
			"""{"country":"US","from":{"postal":"28202"},"to":{"postal":"10001"},"distance":1,"distance_mi":1}""",
		)
		client(stub).postalDistance("28202", "10001", country = "US")
		assertEquals("https://api.parseapi.com/postal/28202/distance/10001?country=US", stub.requests[0].url)
	}
}

class HeadersTest {
	@Test
	fun apiKeyAndUserAgent() = runBlocking {
		val stub = StubTransport(200, DOMAIN_BODY)
		client(stub).domain("x.com")
		val headers = stub.requests[0].headers
		assertEquals("parse_testtesttesttest", headers["X-API-Key"])
		assertEquals("parseapi-kotlin/${ParseAPI.VERSION}", headers["User-Agent"])
		assertNull(headers["X-App-Id"])
	}

	@Test
	fun appIdHeaderWhenSet() = runBlocking {
		val stub = StubTransport(200, DOMAIN_BODY)
		client(stub, appId = "com.example.weather").domain("x.com")
		assertEquals("com.example.weather", stub.requests[0].headers["X-App-Id"])
	}

	@Test
	fun useragentReplacesUa() = runBlocking {
		val stub = StubTransport(200, """{"useragent":"x","bot":false,"mobile":false}""")
		client(stub).useragent("SomeAgent/1.0")
		assertEquals("SomeAgent/1.0", stub.requests[0].headers["User-Agent"])
	}

	@Test
	fun vinDeep() = runBlocking {
		val stub = StubTransport(200, """{"vin":"1HGCM82633A004352","valid":true,"year":2003,"make":"Honda","plant_city":"Marysville","deep":{"recalls":[]}}""")
		val decoded = client(stub).vin("1HGCM82633A004352", deep = true)
		assertEquals("https://api.parseapi.com/vin/1HGCM82633A004352?deep=true", stub.requests[0].url)
		assertEquals(2003, decoded.year)
		assertEquals("Marysville", decoded.plantCity)
		assertEquals(0, decoded.deep?.recalls?.size)
	}
}

class ErrorsTest {
	@Test
	fun honest404() {
		val stub = StubTransport(
			404,
			"""{"code":"not_found","message":"City not found","docs":"https://parseapi.com/docs#not_found","request_id":"req_123"}""",
		)
		val error = assertFailsWith<ParseAPIException> {
			runBlocking { client(stub).city("nowhere") }
		}
		assertEquals(404, error.status)
		assertEquals("not_found", error.code)
		assertEquals("req_123", error.requestId)
		assertEquals("https://parseapi.com/docs#not_found", error.docs)
	}

	@Test
	fun nonJsonErrorBody() {
		val stub = StubTransport(400, "boom")
		val error = assertFailsWith<ParseAPIException> {
			runBlocking { client(stub).country("US") }
		}
		assertEquals("unknown_error", error.code)
		assertEquals(400, error.status)
	}

	@Test
	fun missingKeyThrowsAtConstruction() {
		if (System.getenv("PARSEAPI_KEY") != null) return
		val error = assertFailsWith<ParseAPIException> { ParseAPI() }
		assertEquals("missing_api_key", error.code)
	}
}

class RetriesTest {
	@Test
	fun retriesOn503ThenSucceeds() = runBlocking {
		val stub = StubTransport(
			mutableListOf(
				ParseAPIResponse(503, "{}", emptyMap()),
				ParseAPIResponse(503, "{}", emptyMap()),
				ParseAPIResponse(200, DOMAIN_BODY, emptyMap()),
			),
		)
		val result = client(stub, retries = 2).domain("x.com")
		assertFalse(result.available)
		assertEquals(3, stub.requests.size)
	}

	@Test
	fun noRetryWhenDisabled() {
		val stub = StubTransport(503, "{}")
		val error = assertFailsWith<ParseAPIException> {
			runBlocking { client(stub, retries = 0).domain("x.com") }
		}
		assertEquals(503, error.status)
		assertEquals(1, stub.requests.size)
	}

	@Test
	fun honorsRetryAfterZero() = runBlocking {
		val stub = StubTransport(
			mutableListOf(
				ParseAPIResponse(429, "{}", mapOf("retry-after" to "0")),
				ParseAPIResponse(200, DOMAIN_BODY, emptyMap()),
			),
		)
		client(stub, retries = 2).domain("x.com")
		assertEquals(2, stub.requests.size)
	}

	@Test
	fun no404Retry() {
		val stub = StubTransport(404, """{"code":"not_found","message":"nope"}""")
		assertFailsWith<ParseAPIException> {
			runBlocking { client(stub, retries = 2).country("XX") }
		}
		assertEquals(1, stub.requests.size)
	}

	@Test
	fun retriesNetworkError() = runBlocking {
		var calls = 0
		val transport = ParseAPITransport { request ->
			calls++
			if (calls == 1) throw java.io.IOException("connection reset")
			ParseAPIResponse(200, DOMAIN_BODY, emptyMap())
		}
		val parse = ParseAPI("parse_testtesttesttest", baseUrl = "https://api.parseapi.com", retries = 2, transport = transport)
		val result = parse.domain("x.com")
		assertFalse(result.available)
		assertEquals(2, calls)
	}
}

class DecodingTest {
	@Test
	fun snakeCaseFields() = runBlocking {
		val stub = StubTransport(
			200,
			"""{"ip":"8.8.8.8","country":"US","country_name":"United States","continent":"NA","asn":"AS15169","asn_name":"Google","deep":{"state":"CA","datacenter":true}}""",
		)
		val result = client(stub).ip("8.8.8.8", deep = true)
		assertEquals("United States", result.countryName)
		assertEquals("Google", result.asnName)
		assertEquals("CA", result.deep?.state)
		assertEquals(true, result.deep?.datacenter)
		assertNull(result.deep?.vpn)
	}

	@Test
	fun deepTriad() = runBlocking {
		val locked = StubTransport(200, """{"ip":"8.8.8.8","deep":{}}""")
		val lockedResult = client(locked).ip("8.8.8.8", deep = true)
		assertNotNull(lockedResult.deep)
		assertNull(lockedResult.deep?.datacenter)

		val omitted = StubTransport(200, IP_BODY)
		val omittedResult = client(omitted).ip("8.8.8.8")
		assertNull(omittedResult.deep)
	}

	@Test
	fun nullRegionsArray() = runBlocking {
		val stub = StubTransport(
			200,
			"""{"country":"US","date":"2026-12-25","holiday":{"date":"2026-12-25","name":"Christmas Day","local_name":null,"type":"public","regions":null,"substitute":false}}""",
		)
		val result = client(stub).holidayDate("US", "2026-12-25")
		assertNull(result.holiday?.regions)
		assertEquals("Christmas Day", result.holiday?.name)
	}

	@Test
	fun unknownFieldsIgnored() = runBlocking {
		val stub = StubTransport(200, """{"domain":"x.com","available":false,"some_future_field":{"a":1}}""")
		val result = client(stub).domain("x.com")
		assertEquals("x.com", result.domain)
	}

	@Test
	fun weatherRooms() = runBlocking {
		val stub = StubTransport(
			200,
			"""{"latitude":40.71,"longitude":-74.01,"current":{"temperature":21.7,"temperature_f":71.1,"humidity":63,"observed_at":"2026-08-28T12:51:00Z"},"station":{"id":"KNYC","name":"New York City, Central Park","distance":4.3,"distance_mi":2.7},"source":{"id":"nws","name":"US National Weather Service"}}""",
		)
		val result = client(stub).weather(40.71, -74.01)
		assertEquals(71.1, result.current.temperatureF)
		assertEquals("KNYC", result.station?.id)
		assertEquals("nws", result.source.id)
		assertNull(result.deep)
	}

	@Test
	fun emptyPhoneDeepDecodes() = runBlocking {
		val stub = StubTransport(200, """{"phone":"+14155552671","valid":true,"country":"US","deep":{}}""")
		val result = client(stub).phone("+14155552671", deep = true)
		assertNotNull(result.deep)
		assertTrue(result.valid)
	}
}
