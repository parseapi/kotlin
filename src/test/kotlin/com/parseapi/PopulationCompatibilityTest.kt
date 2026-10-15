package com.parseapi

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertEquals

class PopulationCompatibilityTest {
 @Suppress("DEPRECATION")
 @Test fun retiredMemberKeepsSourceCompatibilityWithoutWireOutput() {
  val json=Json { ignoreUnknownKeys=true; encodeDefaults=true }
  val city=json.decodeFromString<CityDeep>("""{"population":0,"population_period":"2024"}""")
  assertNull(city.populationPeriod)
  assertEquals(0L,city.population)
  assertFalse(json.encodeToString(CityDeep.serializer(),city).contains("population_period"))
  assertFalse(json.encodeToString(CityDeep.serializer(),city).contains("populationPeriod"))
 }
}
