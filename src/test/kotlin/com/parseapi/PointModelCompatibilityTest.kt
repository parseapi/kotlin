package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalSerializationApi::class)
class PointModelCompatibilityTest {
    private val codec = Json { ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase }
	@Test fun pointElectoralAndTerrainAreAvailableWithoutAnotherRequest() = runBlocking {
		val stub = StubTransport(200, """
{"latitude":39.1,"longitude":-104.1,"country":"US","elevation":-5,"elevation_ft":-16.4,"city":null,"electoral":{"code":"0801","name":null,"congress":119}}
""")
		val result = ParseAPI("test") { transport = stub }.point(39.1, -104.1) { deep = true }
		assertEquals("0801", result?.electoral?.code)
		assertNull(result?.electoral?.name)
		assertEquals(119, result?.electoral?.congress)
		assertEquals(-5.0, result?.elevation)
		assertEquals(1, stub.requests.size)
	}

	@Test fun pointElectoralUnknownAndSparseMetadataRemainNullable() {
		for (body in listOf("{}", """
{"electoral":null}
""", """
{"elevation":0}
""")) {
			assertNull(codec.decodeFromString<PointDeep>(body).electoral)
		}
		val sparse = assertNotNull(codec.decodeFromString<PointDeep>("""
{"electoral":{"code":"0000","name":null,"congress":null}}
""").electoral)
		assertEquals("0000", sparse.code)
		assertNull(sparse.name)
		assertNull(sparse.congress)
	}

}
