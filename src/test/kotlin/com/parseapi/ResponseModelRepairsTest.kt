package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalSerializationApi::class)
class ResponseModelRepairsTest {
	private val codec = Json { ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase }

	@Test fun postalCompactTaxDecodesThroughExistingOperation() = runBlocking {
		for (amount in listOf(0.0, 1234.5)) {
			val stub = StubTransport(200, """{"postal":"01234","country":"US","currency":"USD","property_tax":{"annual_median":$amount}}""")
			val result = ParseAPI("test") { transport = stub }.postal("01234") { country = "US"; deep = true }
			assertEquals("01234", result.postal)
			assertEquals(amount, result.propertyTax?.annualMedian)
			assertNull(result.propertyTax?.currency)
			assertNull(result.propertyTax?.period)
			assertEquals("USD", result.currency)
			assertEquals(1, stub.requests.size)
		}
	}

	@Test fun postalUnknownLockedAndOlderTaxShapesRemainReadable() {
		for (suffix in listOf("", """, "deep":null""", """, "deep":{}""", """, "deep":{"property_tax":null}""")) {
			val postal = codec.decodeFromString<Postal>("""{"postal":"01234","country":"US"$suffix}""")
			assertNull(postal.propertyTax)
		}
		val old = codec.decodeFromString<PostalPropertyTax>("""{"annual_median":0,"currency":"USD","period":"2020-2024"}""")
		assertEquals(0.0, old.annualMedian)
		assertEquals("USD", old.currency)
		assertEquals("2020-2024", old.period)
		val compact = codec.decodeFromString<PostalPropertyTax>("""{"annual_median":0}""")
		assertEquals(setOf("annual_median"), codec.parseToJsonElement(codec.encodeToString(compact)).jsonObject.keys)
		val district = codec.decodeFromString<DistrictDeep>("""{"property_tax":{"annual_median":0,"currency":"USD","period":"2020-2024"}}""")
		assertEquals(0.0, district.propertyTax?.annualMedian)
		assertEquals("USD", district.propertyTax?.currency)
		assertEquals("2020-2024", district.propertyTax?.period)
	}

	@Test fun pointElectoralAndTerrainAreAvailableWithoutAnotherRequest() = runBlocking {
		val stub = StubTransport(200, """{"latitude":39.1,"longitude":-104.1,"country":"US","elevation":-5,"elevation_ft":-16.4,"city":null,"electoral":{"code":"0801","name":null,"congress":119}}""")
		val result = ParseAPI("test") { transport = stub }.point(39.1, -104.1) { deep = true }
		assertEquals("0801", result.electoral?.code)
		assertNull(result.electoral?.name)
		assertEquals(119, result.electoral?.congress)
		assertEquals(-5.0, result.elevation)
		assertEquals(1, stub.requests.size)
	}

	@Test fun pointElectoralUnknownAndSparseMetadataRemainNullable() {
		for (body in listOf("{}", """{"electoral":null}""", """{"elevation":0}""")) {
			assertNull(codec.decodeFromString<PointDeep>(body).electoral)
		}
		val sparse = assertNotNull(codec.decodeFromString<PointDeep>("""{"electoral":{"code":"0000","name":null,"congress":null}}""").electoral)
		assertEquals("0000", sparse.code)
		assertNull(sparse.name)
		assertNull(sparse.congress)
	}

	@Test fun addressExistingElectoralBranchIsRetained() {
		for (body in listOf("{}", """{"electoral":null}""")) {
			assertNull(codec.decodeFromString<AddressDeep>(body).electoral)
		}
		val deep = codec.decodeFromString<AddressDeep>("""{"electoral":{"code":"0801","name":null,"congress":119}}""")
		assertEquals("0801", deep.electoral?.code)
		assertNull(deep.electoral?.name)
		assertEquals(119, deep.electoral?.congress)
	}
}
