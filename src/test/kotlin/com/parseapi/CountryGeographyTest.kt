package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class CountryGeographyTest {
	@Test fun decimalsZeroAndNegativeElevation() = runBlocking {
		val body = """{"country":"XX","name":"Test Country","continent":"NA","deep":{"gdp":30769700000000,"gdp_per_capita":90026.5163,"population_growth":-0.25,"urban_population":0,"internet_users":95.5,"land_area":10010.5,"water_area":0,"coastline":0,"elevation":0,"lowest_point":{"name":null,"elevation":-430.5},"highest_point":{"name":"Summit","elevation":8848.86}}}"""
		val country = ParseAPI("test") { transport = StubTransport(200, body) }.country("XX") { deep = true }
		assertEquals(30769700000000.0, country.deep?.gdp)
		assertEquals(90026.5163, country.deep?.gdpPerCapita)
		assertEquals(-0.25, country.deep?.populationGrowth)
		assertEquals(0.0, country.deep?.urbanPopulation)
		assertEquals(95.5, country.deep?.internetUsers)
		assertEquals(10010.5, country.deep?.landArea)
		assertEquals(0.0, country.deep?.waterArea)
		assertEquals(0.0, country.deep?.coastline)
		assertEquals(0.0, country.deep?.elevation)
		assertNull(country.deep?.lowestPoint?.name)
		assertEquals(-430.5, country.deep?.lowestPoint?.elevation)
		assertEquals("Summit", country.deep?.highestPoint?.name)
		assertEquals(8848.86, country.deep?.highestPoint?.elevation)
	}

	@Test fun olderLockedAndNullGeography() = runBlocking {
		for (field in listOf("", """, "deep":{}""", """, "deep":{"land_area":null,"water_area":null,"coastline":null,"elevation":null,"lowest_point":null,"highest_point":null}""")) {
			val body = """{"country":"XX","name":"Test Country","continent":"NA"$field}"""
			val country = ParseAPI("test") { transport = StubTransport(200, body) }.country("XX")
			assertEquals(field.isEmpty(), country.deep == null)
			assertNull(country.deep?.landArea)
			assertNull(country.deep?.waterArea)
			assertNull(country.deep?.coastline)
			assertNull(country.deep?.elevation)
			assertNull(country.deep?.lowestPoint)
			assertNull(country.deep?.highestPoint)
		}
	}
}
