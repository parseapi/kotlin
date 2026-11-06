package com.parseapi

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlin.test.*

@OptIn(ExperimentalSerializationApi::class)
class CityCompactTest {
 @Test fun approvedCityMatchesDecodeWithoutSourceOrScore() {
  val codec = Json { ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase }
  val rows = Json.parseToJsonElement(javaClass.getResource("/noise-approved.json")!!.readText()).jsonArray
  for (row in rows) { for (city in row.jsonObject.getValue("body").jsonObject["data"] as? JsonArray ?: JsonArray(emptyList())) {
   val actual = codec.decodeFromJsonElement<CityResource>(city)
   val match = assertNotNull(actual.match); val expected = city.jsonObject.getValue("match").jsonObject
   assertEquals(expected.getValue("type").jsonPrimitive.content, match.type)
   assertEquals(expected.getValue("value").jsonPrimitive.content, match.value)
   assertEquals(expected["language"]?.jsonPrimitive?.contentOrNull, match.language)
   assertEquals("", match.source); assertNull(match.editDistance)
  } }
 }
}
