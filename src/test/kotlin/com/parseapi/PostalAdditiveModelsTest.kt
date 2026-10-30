package com.parseapi

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalSerializationApi::class)
class PostalAdditiveModelsTest {
 @Test fun alreadyServedFieldsPreserveOldPayloadsAndValues() {
  val codec = Json { ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase }
  val cases = codec.decodeFromString<Map<String, Postal>>(javaClass.getResource("/postal-additive.json")!!.readText())
  for ((name, postal) in cases) {
   val metro = postal.deep!!.metros!![0]
   assertEquals(0.125, metro.share, name)
   if (name == "missing" || name == "null") { assertNull(postal.type, name); assertNull(metro.sharePercent, name) }
  }
  assertEquals("standard", cases["current"]!!.type)
  assertEquals(12.5, cases["current"]!!.deep!!.metros!![0].sharePercent)
  assertEquals(0.0, cases["zero"]!!.deep!!.metros!![0].sharePercent)
  assertEquals("future_custom", cases["future"]!!.type)
  assertEquals(100.0, cases["future"]!!.deep!!.metros!![0].sharePercent)
 }
}
