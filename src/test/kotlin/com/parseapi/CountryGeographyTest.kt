package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class CountryGeographyTest {
	@Test fun decimalsZeroAndNegativeElevation() = runBlocking {
		val body = """
{"country":"XX","name":"Test Country","continent":"NA","gdp_usd":30769700000000,"gdp_per_capita_usd":90026.5163,"population_growth_percent":-0.25,"urban_population_percent":0,"internet_users_percent":95.5,"land_area":10010.5,"water_area":0,"coastline":0,"elevation":0,"lowest_point":{"name":null,"elevation":-430.5},"highest_point":{"name":"Summit","elevation":8848.86}}
"""
		val country = ParseAPI("test") { transport = StubTransport(200, body) }.country("XX") { deep = true }
		assertEquals(30769700000000.0, country?.gdpUsd)
		assertEquals(90026.5163, country?.gdpPerCapitaUsd)
		assertEquals(-0.25, country?.populationGrowthPercent)
		assertEquals(0.0, country?.urbanPopulationPercent)
		assertEquals(95.5, country?.internetUsersPercent)
		assertEquals(10010.5, country?.landArea)
		assertEquals(0.0, country?.waterArea)
		assertEquals(0.0, country?.coastline)
		assertEquals(0.0, country?.elevation)
		assertNull(country?.lowestPoint?.name)
		assertEquals(-430.5, country?.lowestPoint?.elevation)
		assertEquals("Summit", country?.highestPoint?.name)
		assertEquals(8848.86, country?.highestPoint?.elevation)
	}

	@Test fun olderLockedAndNullGeography() = runBlocking {
		for (field in listOf("", """, "deep":{}""", """, "deep":{"land_area":null,"water_area":null,"coastline":null,"elevation":null,"lowest_point":null,"highest_point":null}""")) {
			val body = """{"country":"XX","name":"Test Country","continent":"NA"$field}"""
			val country = ParseAPI("test") { transport = StubTransport(200, body) }.country("XX")
			assertNull(country.lowestPoint)
			assertNull(country?.landArea)
			assertNull(country?.waterArea)
			assertNull(country?.coastline)
			assertNull(country?.elevation)
			assertNull(country?.lowestPoint)
			assertNull(country?.highestPoint)
		}
	}
}
